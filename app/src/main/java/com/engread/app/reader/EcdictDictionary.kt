package com.engread.app.reader

import android.content.Context
import java.io.File
import java.io.PushbackReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.GZIPInputStream
import kotlin.concurrent.thread

class EcdictDictionary(
    context: Context,
    private val autoDownloadExtended: Boolean = true,
) {
    private val appContext = context.applicationContext
    private val extendedFile = File(appContext.filesDir, EXTENDED_FILE_NAME)
    @Volatile private var extendedEntries: Map<String, WordEntry> = emptyMap()

    init {
        if (autoDownloadExtended) loadExtendedDictionaryAsync()
    }

    private val coreEntries: Map<String, WordEntry> by lazy {
        runCatching { loadAssetEntries() }.getOrDefault(emptyMap())
    }

    fun lookup(word: String): WordEntry? {
        val candidates = candidateForms(word)
        val extendedSnapshot = extendedEntries
        for (candidate in candidates) {
            coreEntries[candidate]?.let { return it }
            extendedSnapshot[candidate]?.let { return it }
        }
        return null
    }

    private fun loadExtendedDictionaryAsync() {
        if (!downloadInProgress.compareAndSet(false, true)) return
        thread(name = "EngReadDictionaryDownload", isDaemon = true) {
            try {
                val localEntries = runCatching { loadFileEntries(extendedFile) }.getOrDefault(emptyMap())
                if (localEntries.isNotEmpty()) {
                    extendedEntries = localEntries
                    return@thread
                }
                val downloadedEntries = runCatching { downloadExtendedDictionary() }.getOrDefault(emptyMap())
                if (downloadedEntries.isNotEmpty()) {
                    extendedEntries = downloadedEntries
                }
            } finally {
                downloadInProgress.set(false)
            }
        }
    }

    private fun loadAssetEntries(): Map<String, WordEntry> =
        appContext.assets.open("ecdict_core.tsv").bufferedReader().useLines { lines ->
            lines.mapNotNull(::parseTsvEntry).associateBy { it.word.lowercase(Locale.US) }
        }

    private fun loadFileEntries(file: File): Map<String, WordEntry> {
        if (!file.exists() || file.length() <= 0L) return emptyMap()
        return file.bufferedReader().useLines { lines ->
            lines.mapNotNull(::parseTsvEntry).associateBy { it.word.lowercase(Locale.US) }
        }
    }

    private fun downloadExtendedDictionary(): Map<String, WordEntry> {
        val tempFile = File(appContext.filesDir, "$EXTENDED_FILE_NAME.tmp")
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(ECDICT_CSV_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 45_000
                setRequestProperty("Accept-Encoding", "gzip")
                setRequestProperty("User-Agent", "EngRead-Android")
            }
            val stream = connection.inputStream
            val input = if (connection.contentEncoding.equals("gzip", ignoreCase = true)) {
                GZIPInputStream(stream)
            } else {
                stream
            }
            input.reader(StandardCharsets.UTF_8).use { reader ->
                tempFile.bufferedWriter().use { writer ->
                    writer.appendLine("# Downloaded compact ECDICT entries. Source: $ECDICT_CSV_URL")
                    writer.appendLine("# Fields: word, phonetic, meaning, root/affix, cognates, synonyms.")
                    writeSelectedCsvEntries(reader, writer)
                }
            }
            if (tempFile.length() < 1024L) {
                tempFile.delete()
                return emptyMap()
            }
            if (extendedFile.exists()) extendedFile.delete()
            if (!tempFile.renameTo(extendedFile)) {
                tempFile.copyTo(extendedFile, overwrite = true)
                tempFile.delete()
            }
            return loadFileEntries(extendedFile)
        } finally {
            connection?.disconnect()
            if (tempFile.exists() && tempFile.length() == 0L) tempFile.delete()
        }
    }

    private fun writeSelectedCsvEntries(reader: Reader, writer: java.io.Writer) {
        val csvReader = PushbackReader(reader, 1)
        val header = readCsvRecord(csvReader) ?: return
        val index = header.withIndex().associate { it.value to it.index }
        val seen = hashSetOf<String>()
        while (true) {
            val record = readCsvRecord(csvReader) ?: break
            val word = record.getCsv(index["word"]).lowercase(Locale.US).trim()
            if (!word.isSimpleDictionaryWord() || !seen.add(word)) continue
            val meaning = cleanDownloadedMeaning(
                record.getCsv(index["translation"]).ifBlank { record.getCsv(index["definition"]) },
            )
            if (meaning.isBlank()) continue
            if (!shouldKeepDownloadedEntry(record, index)) continue
            val phonetic = record.getCsv(index["phonetic"]).trim().ifBlank { "未知" }
            writer.append(word.toTsvField())
                .append('\t')
                .append(phonetic.toTsvField())
                .append('\t')
                .append(meaning.toTsvField())
                .append("\t\t\t\n")
        }
    }

    private fun shouldKeepDownloadedEntry(record: List<String>, index: Map<String, Int>): Boolean {
        val rank = listOf(
            record.getCsv(index["bnc"]).toPositiveIntOrNull(),
            record.getCsv(index["frq"]).toPositiveIntOrNull(),
        ).filterNotNull().minOrNull() ?: Int.MAX_VALUE
        val tag = record.getCsv(index["tag"]).lowercase(Locale.US)
        val collins = record.getCsv(index["collins"])
        val oxford = record.getCsv(index["oxford"])
        return rank <= 45_000 ||
            collins.isCoreFlag() ||
            oxford.isCoreFlag() ||
            commonVocabularyTags.any { it in tag }
    }

    private fun parseTsvEntry(line: String): WordEntry? {
        if (line.isBlank() || line.startsWith("#")) return null
        val parts = line.split('\t')
        if (parts.size < 3) return null
        val word = parts[0].trim()
        val phonetic = parts.getOrNull(1).orEmpty().trim()
        val meaning = parts.getOrNull(2).orEmpty().trim()
        if (word.isBlank() || meaning.isBlank()) return null
        return WordEntry(
            word = word,
            phonetic = phonetic.ifBlank { "未知" },
            meaning = meaning,
            root = parts.getOrNull(3).orEmpty().trim(),
            cognates = parts.getOrNull(4).toWordList(),
            synonyms = parts.getOrNull(5).toWordList(),
        )
    }

    private fun candidateForms(word: String): List<String> {
        val base = word.lowercase(Locale.US)
            .replace('’', '\'')
            .trim()
            .trim('\'', '"', '.', ',', ';', ':', '!', '?', '(', ')', '[', ']')
        if (base.isBlank()) return emptyList()
        return buildList {
            add(base)
            contractionForms[base]?.let { addAll(it) }
            irregularForms[base]?.let { addAll(it) }
            if (base.endsWith("'s")) add(base.removeSuffix("'s"))
            if (base.endsWith("s'")) add(base.dropLast(1))
            if (base.endsWith("ies") && base.length > 4) add(base.dropLast(3) + "y")
            if (base.endsWith("ves") && base.length > 4) {
                add(base.dropLast(3) + "f")
                add(base.dropLast(3) + "fe")
            }
            if (base.endsWith("ing") && base.length > 5) {
                val stem = base.dropLast(3)
                add(stem)
                add(stem + "e")
                add(stem.dropDoubledFinalConsonant())
                if (stem.endsWith("y")) add(stem.dropLast(1) + "ie")
            }
            if (base.endsWith("ed") && base.length > 4) {
                val stem = base.dropLast(2)
                add(stem)
                add(base.dropLast(1))
                add(stem.dropDoubledFinalConsonant())
                if (base.endsWith("ied")) add(base.dropLast(3) + "y")
            }
            if (base.endsWith("er") && base.length > 4) {
                val stem = base.dropLast(2)
                add(stem)
                add(stem + "e")
                add(stem.dropDoubledFinalConsonant())
                if (base.endsWith("ier")) add(base.dropLast(3) + "y")
            }
            if (base.endsWith("est") && base.length > 5) {
                val stem = base.dropLast(3)
                add(stem)
                add(stem + "e")
                add(stem.dropDoubledFinalConsonant())
                if (base.endsWith("iest")) add(base.dropLast(4) + "y")
            }
            if (base.endsWith("es") && base.length > 4) add(base.dropLast(2))
            if (base.endsWith("s") && base.length > 3) add(base.dropLast(1))
        }.distinct()
    }

    companion object {
        private const val EXTENDED_FILE_NAME = "ecdict_extended.tsv"
        private const val ECDICT_CSV_URL = "https://raw.githubusercontent.com/skywind3000/ECDICT/master/ecdict.csv"
        private val downloadInProgress = AtomicBoolean(false)
        private val commonVocabularyTags = listOf("zk", "gk", "cet4", "cet6", "ky", "ielts", "toefl", "gre")
        private val contractionForms = mapOf(
            "can't" to listOf("can", "not"),
            "cannot" to listOf("can", "not"),
            "couldn't" to listOf("could", "can", "not"),
            "didn't" to listOf("did", "do", "not"),
            "doesn't" to listOf("does", "do", "not"),
            "don't" to listOf("do", "not"),
            "hadn't" to listOf("had", "have", "not"),
            "hasn't" to listOf("has", "have", "not"),
            "haven't" to listOf("have", "not"),
            "isn't" to listOf("is", "be", "not"),
            "aren't" to listOf("are", "be", "not"),
            "wasn't" to listOf("was", "be", "not"),
            "weren't" to listOf("were", "be", "not"),
            "won't" to listOf("will", "not"),
            "wouldn't" to listOf("would", "will", "not"),
            "shouldn't" to listOf("should", "shall", "not"),
        )
        private val irregularForms = mapOf(
            "am" to listOf("be"),
            "are" to listOf("be"),
            "is" to listOf("be"),
            "was" to listOf("be"),
            "were" to listOf("be"),
            "been" to listOf("be"),
            "being" to listOf("be"),
            "has" to listOf("have"),
            "had" to listOf("have"),
            "does" to listOf("do"),
            "did" to listOf("do"),
            "done" to listOf("do"),
            "went" to listOf("go"),
            "gone" to listOf("go"),
            "got" to listOf("get"),
            "gotten" to listOf("get"),
            "said" to listOf("say"),
            "told" to listOf("tell"),
            "came" to listOf("come"),
            "thought" to listOf("think"),
            "found" to listOf("find"),
            "gave" to listOf("give"),
            "given" to listOf("give"),
            "took" to listOf("take"),
            "taken" to listOf("take"),
            "made" to listOf("make"),
            "saw" to listOf("see"),
            "seen" to listOf("see"),
            "wrote" to listOf("write"),
            "written" to listOf("write"),
            "ran" to listOf("run"),
            "began" to listOf("begin"),
            "begun" to listOf("begin"),
            "brought" to listOf("bring"),
            "felt" to listOf("feel"),
            "heard" to listOf("hear"),
            "kept" to listOf("keep"),
            "left" to listOf("leave"),
            "stood" to listOf("stand"),
            "sent" to listOf("send"),
            "fell" to listOf("fall"),
            "fallen" to listOf("fall"),
            "children" to listOf("child"),
            "men" to listOf("man"),
            "women" to listOf("woman"),
            "people" to listOf("person"),
            "mice" to listOf("mouse"),
            "feet" to listOf("foot"),
            "teeth" to listOf("tooth"),
        )
    }
}

private fun String?.toWordList(): List<String> =
    orEmpty()
        .split("|")
        .map { it.trim() }
        .filter { it.isNotBlank() }

private fun readCsvRecord(reader: PushbackReader): List<String>? {
    val fields = mutableListOf<String>()
    val field = StringBuilder()
    var inQuotes = false
    var sawAny = false
    while (true) {
        val value = reader.read()
        if (value == -1) {
            if (!sawAny && field.isEmpty() && fields.isEmpty()) return null
            fields += field.toString()
            return fields
        }
        sawAny = true
        val char = value.toChar()
        if (inQuotes) {
            if (char == '"') {
                val next = reader.read()
                if (next == '"'.code) {
                    field.append('"')
                } else {
                    inQuotes = false
                    if (next != -1) reader.unread(next)
                }
            } else {
                field.append(char)
            }
        } else {
            when (char) {
                '"' -> if (field.isEmpty()) inQuotes = true else field.append(char)
                ',' -> {
                    fields += field.toString()
                    field.clear()
                }
                '\n' -> {
                    fields += field.toString()
                    return fields
                }
                '\r' -> {
                    val next = reader.read()
                    if (next != '\n'.code && next != -1) reader.unread(next)
                    fields += field.toString()
                    return fields
                }
                else -> field.append(char)
            }
        }
    }
}

private fun List<String>.getCsv(index: Int?): String =
    index?.let { getOrNull(it) }.orEmpty()

private fun String.toPositiveIntOrNull(): Int? =
    trim().toIntOrNull()?.takeIf { it > 0 }

private fun String.isCoreFlag(): Boolean =
    trim().let { it.isNotBlank() && it != "0" }

private fun String.isSimpleDictionaryWord(): Boolean =
    matches(Regex("[a-z][a-z'-]{0,39}"))

private fun cleanDownloadedMeaning(text: String): String =
    text
        .replace("\\r\\n", "\n")
        .replace("\\n", "\n")
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .lines()
        .map { it.replace(Regex("\\s+"), " ").trim() }
        .filter { it.isNotBlank() && !it.startsWith("[网络]") && !it.startsWith("[网路]") }
        .take(4)
        .joinToString("；")
        .trim()

private fun String.toTsvField(): String =
    replace('\t', ' ')
        .replace('\n', '；')
        .replace('\r', ' ')
        .trim()

private fun String.dropDoubledFinalConsonant(): String {
    if (length < 3) return this
    val last = last()
    val previous = this[length - 2]
    if (last != previous || last !in 'b'..'z' || last in "aeiou") return this
    return dropLast(1)
}
