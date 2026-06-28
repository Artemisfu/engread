package com.engread.app.parser

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.text.Html
import android.util.Xml
import com.engread.app.data.BookTocEntry
import com.engread.app.data.SourceType
import io.documentnode.epub4j.domain.Resource
import io.documentnode.epub4j.domain.TOCReference
import io.documentnode.epub4j.epub.EpubReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipException
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import org.xmlpull.v1.XmlPullParser

data class ImportedBook(
    val title: String,
    val fileName: String,
    val sourceType: SourceType,
    val content: String,
    val paragraphs: List<String>,
    val toc: List<BookTocEntry> = emptyList(),
)

class BookImporter(private val context: Context) {
    fun import(uri: Uri): ImportedBook {
        val fileName = context.displayName(uri).ifBlank { "Imported book" }
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("无法读取文件内容")
        val sourceType = detectSourceType(fileName, bytes)
        if (sourceType == SourceType.EPUB) {
            val parsed = EpubParser.parse(bytes, context.cacheDir)
            require(parsed.paragraphs.isNotEmpty()) { "没有解析到可阅读的 EPUB 正文内容" }
            return ImportedBook(
                title = parsed.title.ifBlank { titleFromFileName(fileName) },
                fileName = fileName,
                sourceType = sourceType,
                content = parsed.content,
                paragraphs = parsed.paragraphs,
                toc = parsed.toc,
            )
        }
        val rawText = when (sourceType) {
            SourceType.TXT -> decodeText(bytes)
            SourceType.MOBI -> MobiParser.parse(bytes, context.cacheDir)
            SourceType.EPUB -> error("EPUB 解析分支未处理")
        }
        val cleaned = normalizeBookText(rawText)
        val paragraphs = splitParagraphs(cleaned)
        require(paragraphs.isNotEmpty()) { "没有解析到可阅读的正文内容" }
        return ImportedBook(
            title = titleFromFileName(fileName),
            fileName = fileName,
            sourceType = sourceType,
            content = cleaned,
            paragraphs = paragraphs,
        )
    }
}

private fun Context.displayName(uri: Uri): String {
    var result = ""
    val cursor: Cursor? = runCatching { contentResolver.query(uri, null, null, null, null) }.getOrNull()
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && it.moveToFirst()) {
            result = it.getString(nameIndex).orEmpty()
        }
    }
    return result.ifBlank { uri.lastPathSegment.orEmpty().substringAfterLast('/') }
}

private fun decodeText(bytes: ByteArray): String =
    when {
        bytes.startsWith(0xEF, 0xBB, 0xBF) -> bytes.copyOfRange(3, bytes.size).toString(StandardCharsets.UTF_8)
        bytes.startsWith(0xFF, 0xFE) -> bytes.copyOfRange(2, bytes.size).toString(StandardCharsets.UTF_16LE)
        bytes.startsWith(0xFE, 0xFF) -> bytes.copyOfRange(2, bytes.size).toString(StandardCharsets.UTF_16BE)
        else -> bytes.toString(StandardCharsets.UTF_8).let { utf8 ->
            if ('\uFFFD' in utf8) bytes.toString(Charset.forName("windows-1252")) else utf8
        }
    }

private fun normalizeBookText(text: String): String =
    text
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .replace(Regex("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]"), "")
        .replace(Regex("[ \\t]+\\n"), "\n")
        .trim()

fun splitParagraphs(text: String): List<String> {
    val rough = text.split(Regex("\\n\\s*\\n+"))
    val paragraphs = if (rough.size > 1) {
        rough
    } else {
        text.lines().filter { it.isNotBlank() }
    }
    return paragraphs
        .map { it.replace(Regex("\\s+"), " ").trim() }
        .filter { it.isNotBlank() }
}

private fun titleFromFileName(fileName: String): String =
    fileName.substringBeforeLast('.', fileName).replace('_', ' ').trim().ifBlank { "Untitled" }

private fun detectSourceType(fileName: String, bytes: ByteArray): SourceType {
    val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
    return when {
        extension == "epub" || bytes.looksLikeEpub() -> SourceType.EPUB
        extension in mobiLikeExtensions || bytes.looksLikeMobiContainer() -> SourceType.MOBI
        else -> SourceType.TXT
    }
}

private val mobiLikeExtensions = setOf("mobi", "azw", "azw3", "azw4", "prc")

private fun ByteArray.looksLikeEpub(): Boolean =
    startsWith('P'.code, 'K'.code) &&
        (
            indexOfAscii("application/epub+zip", limit = 1024) >= 0 ||
                runCatching {
                    ZipInputStream(ByteArrayInputStream(this)).use { zip ->
                        var sawContainer = false
                        while (true) {
                            val entry = zip.nextEntry ?: break
                            when (entry.name) {
                                "mimetype" -> {
                                    if (zip.readBytes().toString(StandardCharsets.UTF_8).trim() == "application/epub+zip") {
                                        return@runCatching true
                                    }
                                }
                                "META-INF/container.xml" -> sawContainer = true
                            }
                            zip.closeEntry()
                        }
                        sawContainer
                    }
                }.getOrDefault(false)
        )

private fun ByteArray.looksLikeMobiContainer(): Boolean {
    if (size <= 86) return false
    val recordCount = u16OrNull(76) ?: return false
    if (recordCount < 1) return false
    val firstRecord = u32OrNull(78) ?: return false
    if (hasAsciiAt(firstRecord + 16, "MOBI")) return true
    return indexOfAscii("BOOKMOBI", limit = 4096) >= 0
}

private fun ByteArray.hasAsciiAt(offset: Int, text: String): Boolean {
    if (offset < 0 || offset + text.length > size) return false
    return text.indices.all { index -> this[offset + index].toInt() == text[index].code }
}

private fun ByteArray.indexOfAscii(text: String, limit: Int = size): Int {
    if (text.isEmpty()) return 0
    val end = minOf(size, limit).coerceAtLeast(0)
    if (end < text.length) return -1
    for (offset in 0..(end - text.length)) {
        if (hasAsciiAt(offset, text)) return offset
    }
    return -1
}

private fun ByteArray.startsWith(vararg values: Int): Boolean {
    if (size < values.size) return false
    return values.indices.all { index -> (this[index].toInt() and 0xFF) == values[index] }
}

private object NativeMobiParser {
    init {
        System.loadLibrary("mobi_parser")
    }

    external fun parseFile(path: String): String
}

private data class ParsedEpub(
    val title: String,
    val content: String,
    val paragraphs: List<String>,
    val toc: List<BookTocEntry>,
)

private object EpubParser {
    private data class ManifestItem(
        val id: String,
        val href: String,
        val mediaType: String,
        val properties: String,
    )

    private data class OpfPackage(
        val title: String,
        val manifest: Map<String, ManifestItem>,
        val spineIds: List<String>,
        val ncxHref: String?,
        val navHref: String?,
    )

    private data class TocRef(
        val title: String,
        val href: String,
    )

    private data class NcxPoint(
        var title: String = "",
        var href: String = "",
        val children: MutableList<NcxPoint> = mutableListOf(),
    )

    fun parse(bytes: ByteArray, cacheDir: File): ParsedEpub {
        val lenientResult = runCatching { parseLenient(bytes, cacheDir) }
        return lenientResult.getOrElse { lenientError ->
            runCatching { parseWithEpub4j(bytes) }
                .getOrElse { strictError ->
                    lenientError.addSuppressed(strictError)
                    throw lenientError
                }
        }
    }

    private fun parseWithEpub4j(bytes: ByteArray): ParsedEpub {
        val book = EpubReader().readEpub(ByteArrayInputStream(bytes))
        val title = book.metadata.firstTitle.orEmpty().ifBlank { book.title.orEmpty() }
        val titleByHref = buildTocTitleMap(book.tableOfContents.tocReferences)
        val spineResources = book.spine.spineReferences
            .filter { it.isLinear }
            .mapNotNull { it.resource }
        val resources = spineResources.ifEmpty { book.contents }
            .distinctBy { it.href.orEmpty() }
            .filter { it.isReadableTextResource() }
        require(resources.isNotEmpty()) { "EPUB 没有可阅读的文本章节" }

        val paragraphs = mutableListOf<String>()
        val toc = mutableListOf<BookTocEntry>()
        resources.forEach { resource ->
            val chapterTitle = titleByHref[resource.href.normalizedHref()].orEmpty()
            val resourceParagraphs = splitParagraphs(normalizeBookText(resource.toPlainText()))
            if (chapterTitle.isNotBlank()) {
                val firstBodyParagraphMatchesTitle = resourceParagraphs.firstOrNull()
                    ?.equals(chapterTitle, ignoreCase = true) == true
                val titleParagraphIndex = paragraphs.size
                if (!firstBodyParagraphMatchesTitle) {
                    paragraphs += chapterTitle
                }
                toc += BookTocEntry(
                    title = chapterTitle,
                    paragraphIndex = titleParagraphIndex,
                )
            }
            paragraphs += resourceParagraphs
        }
        val normalizedParagraphs = paragraphs
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotBlank() }
        val normalizedToc = toc
            .mapNotNull { entry ->
                val title = entry.title.trim()
                if (title.isBlank() || normalizedParagraphs.isEmpty()) {
                    null
                } else {
                    entry.copy(
                        title = title,
                        paragraphIndex = entry.paragraphIndex.coerceIn(0, normalizedParagraphs.lastIndex),
                    )
                }
            }
            .distinctBy { it.paragraphIndex to it.title }
        return ParsedEpub(
            title = title,
            content = normalizedParagraphs.joinToString("\n\n"),
            paragraphs = normalizedParagraphs,
            toc = normalizedToc,
        )
    }

    private fun parseLenient(bytes: ByteArray, cacheDir: File): ParsedEpub {
        val entries = readZipEntriesLenient(bytes, cacheDir)
        val containerPath = "META-INF/container.xml"
        val opfPath = entries[containerPath]?.let { data ->
            parseContainerRootfile(data.toText())
        } ?: entries.keys.firstOrNull { it.endsWith(".opf", ignoreCase = true) }.orEmpty()
        require(opfPath.isNotBlank()) { "EPUB 没有找到 OPF 包描述文件" }
        val opfBytes = entries[opfPath] ?: error("EPUB 缺少 OPF 包描述文件")
        val opf = parseOpf(opfBytes.toText())
        val opfDir = opfPath.substringBeforeLast('/', "")
        val tocRefs = buildList {
            opf.ncxHref
                ?.let { resolveZipPath(opfDir, it) }
                ?.let { path -> entries[path]?.toText()?.let { addAll(parseNcxToc(it, path.substringBeforeLast('/', ""))) } }
            if (isEmpty()) {
                opf.navHref
                    ?.let { resolveZipPath(opfDir, it) }
                    ?.let { path -> entries[path]?.toText()?.let { addAll(parseNavToc(it, path.substringBeforeLast('/', ""))) } }
            }
        }
        val tocByHref = linkedMapOf<String, String>()
        tocRefs.forEach { ref ->
            tocByHref.putIfAbsent(ref.href.normalizedHref(), ref.title)
        }
        val spinePaths = opf.spineIds
            .mapNotNull { id -> opf.manifest[id] }
            .map { item -> resolveZipPath(opfDir, item.href) }
            .filter { path -> entries.containsKey(path) }
        val resources = spinePaths.ifEmpty {
            entries.keys
                .filter { it.isReadableTextPath() }
                .sorted()
        }.distinct()
        require(resources.isNotEmpty()) { "EPUB 没有可阅读的文本章节" }

        val paragraphs = mutableListOf<String>()
        val toc = mutableListOf<BookTocEntry>()
        resources.forEach { path ->
            val chapterTitle = tocByHref[path.normalizedHref()].orEmpty()
            val resourceData = entries[path] ?: ByteArray(0)
            val resourceParagraphs = splitParagraphs(normalizeBookText(resourceData.toHtmlPlainText()))
            if (chapterTitle.isNotBlank()) {
                val firstBodyParagraphMatchesTitle = resourceParagraphs.firstOrNull()
                    ?.equals(chapterTitle, ignoreCase = true) == true
                val titleParagraphIndex = paragraphs.size
                if (!firstBodyParagraphMatchesTitle) {
                    paragraphs += chapterTitle
                }
                toc += BookTocEntry(
                    title = chapterTitle,
                    paragraphIndex = titleParagraphIndex,
                )
            }
            paragraphs += resourceParagraphs
        }
        val normalizedParagraphs = paragraphs
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotBlank() }
        val normalizedToc = toc
            .mapNotNull { entry ->
                val title = entry.title.trim()
                if (title.isBlank() || normalizedParagraphs.isEmpty()) {
                    null
                } else {
                    entry.copy(
                        title = title,
                        paragraphIndex = entry.paragraphIndex.coerceIn(0, normalizedParagraphs.lastIndex),
                    )
                }
            }
            .distinctBy { it.paragraphIndex to it.title }
        return ParsedEpub(
            title = opf.title,
            content = normalizedParagraphs.joinToString("\n\n"),
            paragraphs = normalizedParagraphs,
            toc = normalizedToc,
        )
    }

    private fun readZipEntriesLenient(bytes: ByteArray, cacheDir: File): Map<String, ByteArray> {
        val tempFile = File.createTempFile("engread-epub-", ".epub", cacheDir)
        try {
            tempFile.writeBytes(bytes)
            ZipFile(tempFile).use { zip ->
                val result = linkedMapOf<String, ByteArray>()
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.isDirectory) continue
                    val name = entry.name.normalizedZipPath()
                    if (name.isBlank()) continue
                    if (!name.isEpubMetadataOrTextPath()) continue
                    val data = zip.readEntryBytesLenient(entry)
                    if (data.isNotEmpty()) result[name] = data
                }
                return result
            }
        } finally {
            tempFile.delete()
        }
    }

    private fun ZipFile.readEntryBytesLenient(entry: java.util.zip.ZipEntry): ByteArray {
        val output = ByteArrayOutputStream()
        val input = getInputStream(entry)
        try {
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = try {
                    input.read(buffer)
                } catch (error: IOException) {
                    if (output.size() > 0 && error.isCrcFailure()) break else throw error
                }
                if (read < 0) break
                if (read > 0) output.write(buffer, 0, read)
            }
        } finally {
            runCatching { input.close() }
                .onFailure { error ->
                    if (!error.isCrcFailure() && output.size() == 0) throw error
                }
        }
        return output.toByteArray()
    }

    private fun Throwable.isCrcFailure(): Boolean =
        this is ZipException || message.orEmpty().contains("crc", ignoreCase = true)

    private fun parseContainerRootfile(xml: String): String {
        val parser = xml.newXmlParser()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.tagName() == "rootfile") {
                val path = parser.attribute("full-path").orEmpty().normalizedZipPath()
                if (path.isNotBlank()) return path
            }
        }
        return ""
    }

    private fun parseOpf(xml: String): OpfPackage {
        val parser = xml.newXmlParser()
        val manifest = linkedMapOf<String, ManifestItem>()
        val spineIds = mutableListOf<String>()
        var inMetadata = false
        var title = ""
        var spineTocId = ""
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.tagName()) {
                        "metadata" -> inMetadata = true
                        "title" -> if (inMetadata && title.isBlank()) {
                            title = runCatching { parser.nextText().trim() }.getOrDefault("")
                        }
                        "item" -> {
                            val id = parser.attribute("id").orEmpty()
                            val href = parser.attribute("href").orEmpty()
                            if (id.isNotBlank() && href.isNotBlank()) {
                                manifest[id] = ManifestItem(
                                    id = id,
                                    href = href,
                                    mediaType = parser.attribute("media-type").orEmpty(),
                                    properties = parser.attribute("properties").orEmpty(),
                                )
                            }
                        }
                        "spine" -> spineTocId = parser.attribute("toc").orEmpty()
                        "itemref" -> {
                            val idref = parser.attribute("idref").orEmpty()
                            if (idref.isNotBlank()) spineIds += idref
                        }
                    }
                }
                XmlPullParser.END_TAG -> if (parser.tagName() == "metadata") inMetadata = false
            }
        }
        val ncxHref = manifest[spineTocId]?.href
            ?: manifest.values.firstOrNull { item ->
                item.mediaType.equals("application/x-dtbncx+xml", ignoreCase = true) ||
                    item.id.equals("ncx", ignoreCase = true)
            }?.href
        val navHref = manifest.values.firstOrNull { item ->
            item.properties.split(Regex("\\s+")).any { it.equals("nav", ignoreCase = true) }
        }?.href
        return OpfPackage(
            title = title,
            manifest = manifest,
            spineIds = spineIds,
            ncxHref = ncxHref,
            navHref = navHref,
        )
    }

    private fun parseNcxToc(xml: String, baseDir: String): List<TocRef> {
        val parser = xml.newXmlParser()
        val roots = mutableListOf<NcxPoint>()
        val stack = mutableListOf<NcxPoint>()
        var navLabelDepth = 0
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.tagName()) {
                        "navPoint" -> stack += NcxPoint()
                        "navLabel" -> navLabelDepth += 1
                        "text" -> if (navLabelDepth > 0 && stack.isNotEmpty() && stack.last().title.isBlank()) {
                            stack.last().title = runCatching { parser.nextText().trim() }.getOrDefault("")
                        }
                        "content" -> if (stack.isNotEmpty()) {
                            stack.last().href = parser.attribute("src").orEmpty()
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.tagName()) {
                        "navLabel" -> navLabelDepth = (navLabelDepth - 1).coerceAtLeast(0)
                        "navPoint" -> {
                            val point = stack.removeLastOrNull() ?: continue
                            if (stack.isEmpty()) {
                                roots += point
                            } else {
                                stack.last().children += point
                            }
                        }
                    }
                }
            }
        }
        val result = mutableListOf<TocRef>()
        fun visit(point: NcxPoint) {
            val title = point.title.trim()
            val href = point.href.trim()
            if (title.isNotBlank() && href.isNotBlank()) {
                result += TocRef(title = title, href = resolveZipPath(baseDir, href))
            }
            point.children.forEach(::visit)
        }
        roots.forEach(::visit)
        return result
    }

    private fun parseNavToc(xml: String, baseDir: String): List<TocRef> {
        val parser = xml.newXmlParser()
        val tocLinks = mutableListOf<TocRef>()
        val allLinks = mutableListOf<TocRef>()
        var depth = 0
        var tocNavDepth: Int? = null
        var currentHref: String? = null
        var currentText: StringBuilder? = null
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    depth += 1
                    val tag = parser.tagName()
                    if (tag == "nav" && tocNavDepth == null && parser.anyAttributeContains("toc")) {
                        tocNavDepth = depth
                    } else if (tag == "a") {
                        currentHref = parser.attribute("href")
                        currentText = StringBuilder()
                    }
                }
                XmlPullParser.TEXT -> currentText?.append(parser.text)
                XmlPullParser.END_TAG -> {
                    val tag = parser.tagName()
                    if (tag == "a") {
                        val href = currentHref.orEmpty()
                        val title = currentText?.toString().orEmpty().replace(Regex("\\s+"), " ").trim()
                        if (href.isNotBlank() && title.isNotBlank()) {
                            val ref = TocRef(title = title, href = resolveZipPath(baseDir, href))
                            if (tocNavDepth != null && depth > tocNavDepth) tocLinks += ref else allLinks += ref
                        }
                        currentHref = null
                        currentText = null
                    }
                    if (tag == "nav" && tocNavDepth == depth) tocNavDepth = null
                    depth = (depth - 1).coerceAtLeast(0)
                }
            }
        }
        return tocLinks.ifEmpty { allLinks }
    }

    private fun String.newXmlParser(): XmlPullParser =
        Xml.newPullParser().also { parser ->
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(byteInputStream(), StandardCharsets.UTF_8.name())
        }

    private fun XmlPullParser.tagName(): String =
        name.orEmpty().substringAfter(':')

    private fun XmlPullParser.attribute(name: String): String? {
        for (index in 0 until attributeCount) {
            if (getAttributeName(index).orEmpty().substringAfter(':') == name) {
                return getAttributeValue(index)
            }
        }
        return null
    }

    private fun XmlPullParser.anyAttributeContains(value: String): Boolean {
        for (index in 0 until attributeCount) {
            if (getAttributeValue(index).orEmpty().contains(value, ignoreCase = true)) return true
        }
        return false
    }

    private fun ByteArray.toText(): String = decodeText(this)

    private fun ByteArray.toHtmlPlainText(): String = toText().toHtmlPlainText()

    private fun String.toHtmlPlainText(): String {
        val withoutHiddenBlocks = replace(Regex("(?is)<script\\b[^>]*>.*?</script>"), " ")
            .replace(Regex("(?is)<style\\b[^>]*>.*?</style>"), " ")
            .replace(Regex("(?is)<head\\b[^>]*>.*?</head>"), " ")
        return Html.fromHtml(withoutHiddenBlocks, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    private fun resolveZipPath(baseDir: String, href: String): String {
        val cleanHref = href.substringBefore('#').trim().replace('\\', '/')
        val decodedHref = runCatching {
            URLDecoder.decode(cleanHref, StandardCharsets.UTF_8.name())
        }.getOrDefault(cleanHref)
        val rawPath = if (decodedHref.startsWith('/')) {
            decodedHref.dropWhile { it == '/' }
        } else if (baseDir.isBlank()) {
            decodedHref
        } else {
            "$baseDir/$decodedHref"
        }
        val segments = mutableListOf<String>()
        rawPath.split('/').forEach { segment ->
            when (segment) {
                "", "." -> Unit
                ".." -> if (segments.isNotEmpty()) segments.removeAt(segments.lastIndex)
                else -> segments += segment
            }
        }
        return segments.joinToString("/")
    }

    private fun String.normalizedZipPath(): String =
        replace('\\', '/').trim().trimStart('/')

    private fun String.isReadableTextPath(): Boolean {
        val path = normalizedHref().lowercase(Locale.ROOT)
        return path.endsWith(".xhtml") || path.endsWith(".html") || path.endsWith(".htm")
    }

    private fun String.isEpubMetadataOrTextPath(): Boolean {
        val path = normalizedHref().lowercase(Locale.ROOT)
        return path == "mimetype" ||
            path.endsWith(".xml") ||
            path.endsWith(".opf") ||
            path.endsWith(".ncx") ||
            path.endsWith(".xhtml") ||
            path.endsWith(".html") ||
            path.endsWith(".htm")
    }

    private fun buildTocTitleMap(references: List<TOCReference>): Map<String, String> {
        val result = linkedMapOf<String, String>()
        fun visit(reference: TOCReference) {
            val href = reference.resource?.href.normalizedHref()
            val title = reference.title.orEmpty().trim()
            if (href.isNotBlank() && title.isNotBlank()) {
                result.putIfAbsent(href, title)
            }
            reference.children.forEach(::visit)
        }
        references.forEach(::visit)
        return result
    }

    private fun Resource.isReadableTextResource(): Boolean {
        val href = href.orEmpty().lowercase()
        val mediaTypeName = mediaType?.name.orEmpty().lowercase()
        return href.endsWith(".xhtml") ||
            href.endsWith(".html") ||
            href.endsWith(".htm") ||
            mediaTypeName.contains("html") ||
            mediaTypeName.contains("xml") ||
            mediaTypeName.startsWith("text/")
    }

    private fun Resource.toPlainText(): String {
        val raw = getData().toString(inputEncoding?.let { Charset.forName(it) } ?: StandardCharsets.UTF_8)
        return raw.toHtmlPlainText()
    }

    private fun String?.normalizedHref(): String =
        orEmpty()
            .substringBefore('#')
            .trim()
            .replace('\\', '/')
}

object MobiParser {
    fun parse(bytes: ByteArray, cacheDir: File? = null): String {
        val nativeText = runCatching {
            require(cacheDir != null) { "缺少临时目录" }
            val tempFile = File.createTempFile("engread-", ".mobi", cacheDir)
            try {
                tempFile.writeBytes(bytes)
                NativeMobiParser.parseFile(tempFile.absolutePath)
            } finally {
                tempFile.delete()
            }
        }.getOrNull()
        if (!nativeText.isNullOrBlank()) {
            return Html.fromHtml(nativeText, Html.FROM_HTML_MODE_LEGACY).toString()
        }

        require(bytes.size > 86) { "MOBI 文件过小或已损坏" }
        val recordCount = bytes.u16(76)
        require(recordCount > 1) { "MOBI 没有正文记录" }

        val recordOffsets = (0 until recordCount).map { index ->
            bytes.u32(78 + index * 8)
        }.filter { it in bytes.indices }
        require(recordOffsets.size > 1) { "MOBI 记录表损坏" }

        val firstRecord = recordOffsets.first()
        require(firstRecord + 16 < bytes.size) { "MOBI 头部损坏" }

        val compression = bytes.u16(firstRecord)
        val textLength = bytes.u32(firstRecord + 4)
        val textRecordCount = bytes.u16(firstRecord + 8).coerceAtMost(recordOffsets.lastIndex)
        val mobiHeaderOffset = firstRecord + 16
        val hasMobiHeader = mobiHeaderOffset + 4 <= bytes.size &&
            bytes.copyOfRange(mobiHeaderOffset, mobiHeaderOffset + 4).toString(StandardCharsets.US_ASCII) == "MOBI"
        require(hasMobiHeader) { "不是有效的 MOBI 文本文件" }
        require(compression == 1 || compression == 2) {
            "暂不支持这种 MOBI 压缩或 DRM 格式"
        }
        val textCharset = when (bytes.u32(mobiHeaderOffset + 12)) {
            1252 -> Charset.forName("windows-1252")
            65001 -> StandardCharsets.UTF_8
            else -> StandardCharsets.UTF_8
        }

        val textBytes = buildList {
            for (recordIndex in 1..textRecordCount) {
                val start = recordOffsets.getOrNull(recordIndex) ?: continue
                val end = recordOffsets.getOrNull(recordIndex + 1) ?: bytes.size
                if (start < end && end <= bytes.size) {
                    val record = bytes.copyOfRange(start, end)
                    add(if (compression == 2) decompressPalmDoc(record) else record)
                }
            }
        }.fold(ByteArray(0)) { acc, item -> acc + item }
            .let { if (textLength > 0 && it.size > textLength) it.copyOf(textLength) else it }

        val htmlLike = textBytes.toString(textCharset)
        val plain = if ('<' in htmlLike && '>' in htmlLike) {
            Html.fromHtml(htmlLike, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            htmlLike
        }
        return plain
    }

    private fun decompressPalmDoc(input: ByteArray): ByteArray {
        val output = ArrayList<Byte>(input.size * 2)
        var index = 0
        while (index < input.size) {
            val value = input[index].toInt() and 0xFF
            when (value) {
                0 -> {
                    output.add(0.toByte())
                    index += 1
                }

                in 1..8 -> {
                    val count = value.coerceAtMost(input.size - index - 1)
                    repeat(count) { offset -> output.add(input[index + 1 + offset]) }
                    index += 1 + count
                }

                in 9..0x7F -> {
                    output.add(value.toByte())
                    index += 1
                }

                in 0x80..0xBF -> {
                    if (index + 1 >= input.size) {
                        index += 1
                    } else {
                        val pair = (value shl 8) or (input[index + 1].toInt() and 0xFF)
                        val distance = (pair shr 3) and 0x07FF
                        val length = (pair and 0x0007) + 3
                        repeat(length) {
                            val source = output.size - distance
                            if (source in output.indices) {
                                output.add(output[source])
                            }
                        }
                        index += 2
                    }
                }

                else -> {
                    output.add(' '.code.toByte())
                    output.add((value xor 0x80).toByte())
                    index += 1
                }
            }
        }
        return output.toByteArray()
    }
}

private fun ByteArray.u16(offset: Int): Int {
    require(offset + 1 < size) { "文件结构损坏" }
    return ((this[offset].toInt() and 0xFF) shl 8) or (this[offset + 1].toInt() and 0xFF)
}

private fun ByteArray.u16OrNull(offset: Int): Int? {
    if (offset < 0 || offset + 1 >= size) return null
    return ((this[offset].toInt() and 0xFF) shl 8) or (this[offset + 1].toInt() and 0xFF)
}

private fun ByteArray.u32(offset: Int): Int {
    require(offset + 3 < size) { "文件结构损坏" }
    val value = ((this[offset].toLong() and 0xFF) shl 24) or
        ((this[offset + 1].toLong() and 0xFF) shl 16) or
        ((this[offset + 2].toLong() and 0xFF) shl 8) or
        (this[offset + 3].toLong() and 0xFF)
    return value.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

private fun ByteArray.u32OrNull(offset: Int): Int? {
    if (offset < 0 || offset + 3 >= size) return null
    val value = ((this[offset].toLong() and 0xFF) shl 24) or
        ((this[offset + 1].toLong() and 0xFF) shl 16) or
        ((this[offset + 2].toLong() and 0xFF) shl 8) or
        (this[offset + 3].toLong() and 0xFF)
    return value.takeIf { it <= Int.MAX_VALUE }?.toInt()
}
