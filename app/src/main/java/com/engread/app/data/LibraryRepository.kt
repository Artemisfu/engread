package com.engread.app.data

import android.content.Context
import android.net.Uri
import com.engread.app.parser.BookImporter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LibraryRepository(
    private val context: Context,
    private val importer: BookImporter = BookImporter(context),
) {
    private val libraryFile = File(context.filesDir, "engread-library.json")
    private val notesFile = File(context.filesDir, "engread-notes.json")
    private val lookupHistoryFile = File(context.filesDir, "engread-lookup-history.json")
    private val chatsFile = File(context.filesDir, "engread-chats.json")
    private val settingsFile = File(context.filesDir, "engread-settings.json")
    private val progressFile = File(context.filesDir, "engread-progress.json")

    @Synchronized
    fun getBooks(): List<Book> {
        if (!libraryFile.exists()) return emptyList()
        val root = runCatching { JSONObject(libraryFile.readText()) }.getOrNull() ?: return emptyList()
        val items = root.optJSONArray("books") ?: JSONArray()
        val progressByBook = getProgress()
        val parsedBooks = buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                runCatching { item.toBook() }.getOrNull()?.let { book -> add(book) }
            }
        }
        val readableBooks = parsedBooks.filterNot { it.isLegacyRawMobiTextImport() }
        if (readableBooks.size != parsedBooks.size) {
            saveBooks(readableBooks)
            saveProgress(getProgress().filterKeys { bookId -> readableBooks.any { it.id == bookId } })
        }
        return readableBooks
            .map { book ->
                val progress = progressByBook[book.id]
                if (progress == null) {
                    book
                } else {
                    book.copy(
                        lastReadParagraph = progress.lastReadParagraph.coerceIn(
                            0,
                            (book.paragraphs.size - 1).coerceAtLeast(0),
                        ),
                        updatedAt = progress.updatedAt,
                    )
                }
            }
            .sortedByDescending { it.updatedAt }
    }

    @Synchronized
    fun getBook(bookId: String): Book? = getBooks().firstOrNull { it.id == bookId }

    @Synchronized
    fun importBook(uri: Uri): Book {
        val imported = importer.import(uri)
        val now = System.currentTimeMillis()
        val book = Book(
            id = UUID.randomUUID().toString(),
            title = imported.title,
            fileName = imported.fileName,
            sourceType = imported.sourceType,
            content = imported.content,
            paragraphs = imported.paragraphs,
            toc = imported.toc,
            addedAt = now,
            updatedAt = now,
            lastReadParagraph = 0,
        )
        saveBooks(getBooks() + book)
        return book
    }

    @Synchronized
    fun deleteBook(bookId: String) {
        saveBooks(getBooks().filterNot { it.id == bookId })
        saveNotes(getNotes().filterNot { it.bookId == bookId })
        saveLookupHistory(getLookupHistory().filterNot { it.bookId == bookId })
        saveBookChats(getBookChats().filterNot { it.bookId == bookId })
        saveProgress(getProgress().filterKeys { it != bookId })
    }

    @Synchronized
    fun updateProgress(bookId: String, paragraphIndex: Int) {
        val now = System.currentTimeMillis()
        val next = getProgress().toMutableMap()
        next[bookId] = StoredProgress(
            lastReadParagraph = paragraphIndex.coerceAtLeast(0),
            updatedAt = now,
        )
        saveProgress(next)
    }

    @Synchronized
    fun getSettings(): ReaderSettings {
        if (!settingsFile.exists()) return ReaderSettings()
        val root = runCatching { JSONObject(settingsFile.readText()) }.getOrNull() ?: return ReaderSettings()
        return ReaderSettings(
            font = root.optString("font").toEnumOrDefault(ReaderFont.SANS),
            noteFont = root.optString("noteFont").toEnumOrDefault(ReaderFont.SANS),
            fontSizeSp = root.optInt("fontSizeSp", 19).coerceIn(14, 30),
            theme = root.optString("theme").toEnumOrDefault(ReaderTheme.LIGHT),
            translation = root.optJSONObject("translation").toTranslationSettings(),
        )
    }

    @Synchronized
    fun saveSettings(settings: ReaderSettings) {
        val root = JSONObject()
            .put("font", settings.font.name)
            .put("noteFont", settings.noteFont.name)
            .put("fontSizeSp", settings.fontSizeSp)
            .put("theme", settings.theme.name)
            .put("translation", settings.translation.toJson())
        settingsFile.writeText(root.toString(2))
    }

    @Synchronized
    fun getNotes(): List<ReaderNote> {
        if (!notesFile.exists()) return emptyList()
        val root = runCatching { JSONObject(notesFile.readText()) }.getOrNull() ?: return emptyList()
        val items = root.optJSONArray("notes") ?: JSONArray()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                runCatching { item.toNote() }.getOrNull()?.let { add(it) }
            }
        }.sortedByDescending { it.updatedAt }
    }

    @Synchronized
    fun addNote(
        book: Book,
        paragraphIndex: Int,
        sentence: String,
        translationText: String,
        noteText: String,
    ): ReaderNote {
        val now = System.currentTimeMillis()
        val note = ReaderNote(
            id = UUID.randomUUID().toString(),
            bookId = book.id,
            bookTitle = book.title,
            paragraphIndex = paragraphIndex,
            sentence = sentence.trim(),
            translationText = translationText.trim(),
            noteText = noteText.trim(),
            createdAt = now,
            updatedAt = now,
        )
        saveNotes(getNotes() + note)
        return note
    }

    @Synchronized
    fun updateNote(noteId: String, noteText: String) {
        val now = System.currentTimeMillis()
        saveNotes(
            getNotes().map { note ->
                if (note.id == noteId) note.copy(noteText = noteText.trim(), updatedAt = now) else note
            },
        )
    }

    @Synchronized
    fun deleteNote(noteId: String) {
        saveNotes(getNotes().filterNot { it.id == noteId })
    }

    @Synchronized
    fun restoreNote(note: ReaderNote) {
        saveNotes(listOf(note) + getNotes().filterNot { it.id == note.id })
    }

    @Synchronized
    fun getLookupHistory(): List<LookupHistoryEntry> {
        if (!lookupHistoryFile.exists()) return emptyList()
        val root = runCatching { JSONObject(lookupHistoryFile.readText()) }.getOrNull() ?: return emptyList()
        val items = root.optJSONArray("history") ?: JSONArray()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                runCatching { item.toLookupHistoryEntry() }.getOrNull()?.let { add(it) }
            }
        }.sortedByDescending { it.updatedAt }
    }

    @Synchronized
    fun addLookupHistory(
        book: Book,
        paragraphIndex: Int,
        type: LookupHistoryType,
        sourceText: String,
        resultText: String,
        phonetic: String = "",
    ): LookupHistoryEntry {
        val now = System.currentTimeMillis()
        val source = sourceText.trim()
        val currentHistory = getLookupHistory()
        val existing = currentHistory.firstOrNull { entry ->
            entry.type == type && entry.lookupKey() == lookupHistoryKey(type, source)
        }
        val entry = existing?.copy(
            bookId = book.id,
            bookTitle = book.title,
            paragraphIndex = paragraphIndex.coerceAtLeast(0),
            sourceText = source,
            resultText = resultText.trim(),
            phonetic = phonetic.trim(),
            updatedAt = now,
        ) ?: LookupHistoryEntry(
            id = UUID.randomUUID().toString(),
            type = type,
            bookId = book.id,
            bookTitle = book.title,
            paragraphIndex = paragraphIndex.coerceAtLeast(0),
            sourceText = source,
            resultText = resultText.trim(),
            phonetic = phonetic.trim(),
            createdAt = now,
            updatedAt = now,
        )
        saveLookupHistory((listOf(entry) + currentHistory.filterNot { it.id == entry.id }).take(300))
        return entry
    }

    @Synchronized
    fun clearLookupHistory() {
        saveLookupHistory(emptyList())
    }

    @Synchronized
    fun deleteLookupHistory(id: String) {
        saveLookupHistory(getLookupHistory().filterNot { it.id == id })
    }

    @Synchronized
    fun restoreLookupHistory(entry: LookupHistoryEntry) {
        saveLookupHistory((listOf(entry) + getLookupHistory().filterNot { it.id == entry.id }).take(300))
    }

    @Synchronized
    fun getBookChats(): List<BookChat> {
        if (!chatsFile.exists()) return emptyList()
        val root = runCatching { JSONObject(chatsFile.readText()) }.getOrNull() ?: return emptyList()
        val items = root.optJSONArray("chats") ?: JSONArray()
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                runCatching { item.toBookChat() }.getOrNull()?.let { add(it) }
            }
        }.sortedByDescending { it.updatedAt }
    }

    @Synchronized
    fun getBookChat(bookId: String): BookChat? =
        getBookChats().firstOrNull { it.bookId == bookId }

    @Synchronized
    fun hasBookChat(bookId: String): Boolean {
        val chat = getBookChat(bookId) ?: return false
        return chat.summary.isNotBlank() || chat.messages.isNotEmpty()
    }

    @Synchronized
    fun saveBookChat(book: Book, summary: String, messages: List<ChatMessage>): BookChat {
        val now = System.currentTimeMillis()
        val chat = BookChat(
            bookId = book.id,
            bookTitle = book.title,
            summary = summary.trim(),
            messages = messages,
            updatedAt = now,
        )
        saveBookChats(listOf(chat) + getBookChats().filterNot { it.bookId == book.id })
        return chat
    }

    fun buildNotesMarkdown(notes: List<ReaderNote> = getNotes()): String {
        if (notes.isEmpty()) {
            return "# EngRead Notes\n\n暂无笔记。\n"
        }
        return buildString {
            appendLine("# EngRead Notes")
            appendLine()
            notes.groupBy { it.bookTitle }.forEach { (bookTitle, bookNotes) ->
                appendLine("## $bookTitle")
                appendLine()
                bookNotes.sortedByDescending { it.updatedAt }.forEach { note ->
                    appendLine("- 原句：${note.sentence}")
                    if (note.translationText.isNotBlank()) {
                        appendLine("  译文：${note.translationText}")
                    }
                    if (note.noteText.isNotBlank()) {
                        appendLine("  笔记：${note.noteText}")
                    }
                    appendLine("  位置：第 ${note.paragraphIndex + 1} 段")
                    appendLine("  时间：${formatExportTimestamp(note.updatedAt)}")
                    appendLine()
                }
            }
        }
    }

    private fun saveBooks(books: List<Book>) {
        val root = JSONObject().put("books", JSONArray().also { array ->
            books.forEach { array.put(it.toJson()) }
        })
        libraryFile.writeText(root.toString(2))
    }

    private fun saveNotes(notes: List<ReaderNote>) {
        val root = JSONObject().put("notes", JSONArray().also { array ->
            notes.forEach { array.put(it.toJson()) }
        })
        notesFile.writeText(root.toString(2))
    }

    private fun saveLookupHistory(history: List<LookupHistoryEntry>) {
        val root = JSONObject().put("history", JSONArray().also { array ->
            history.forEach { array.put(it.toJson()) }
        })
        lookupHistoryFile.writeText(root.toString(2))
    }

    private fun saveBookChats(chats: List<BookChat>) {
        val root = JSONObject().put("chats", JSONArray().also { array ->
            chats.forEach { array.put(it.toJson()) }
        })
        chatsFile.writeText(root.toString(2))
    }

    private fun getProgress(): Map<String, StoredProgress> {
        if (!progressFile.exists()) return emptyMap()
        val root = runCatching { JSONObject(progressFile.readText()) }.getOrNull() ?: return emptyMap()
        val items = root.optJSONObject("progress") ?: JSONObject()
        return buildMap {
            val keys = items.keys()
            while (keys.hasNext()) {
                val bookId = keys.next()
                val item = items.optJSONObject(bookId) ?: continue
                put(
                    bookId,
                    StoredProgress(
                        lastReadParagraph = item.optInt("lastReadParagraph", 0),
                        updatedAt = item.optLong("updatedAt", 0L),
                    ),
                )
            }
        }
    }

    private fun saveProgress(progress: Map<String, StoredProgress>) {
        val items = JSONObject()
        progress.forEach { (bookId, value) ->
            items.put(
                bookId,
                JSONObject()
                    .put("lastReadParagraph", value.lastReadParagraph)
                    .put("updatedAt", value.updatedAt),
            )
        }
        progressFile.writeText(JSONObject().put("progress", items).toString(2))
    }
}

private data class StoredProgress(
    val lastReadParagraph: Int,
    val updatedAt: Long,
)

private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
    runCatching { enumValueOf<T>(this) }.getOrDefault(default)

private fun JSONObject?.toTranslationSettings(): TranslationSettings =
    TranslationSettings(
        baseUrl = this?.optString("baseUrl", "https://api.openai.com/v1").orEmpty()
            .ifBlank { "https://api.openai.com/v1" },
        apiKey = this?.optString("apiKey", "").orEmpty(),
        model = this?.optString("model", "gpt-4o-mini").orEmpty().ifBlank { "gpt-4o-mini" },
    )

private fun TranslationSettings.toJson(): JSONObject =
    JSONObject()
        .put("baseUrl", baseUrl)
        .put("apiKey", apiKey)
        .put("model", model)

private fun Book.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("title", title)
        .put("fileName", fileName)
        .put("sourceType", sourceType.name)
        .put("content", content)
        .put("paragraphs", JSONArray().also { array -> paragraphs.forEach { array.put(it) } })
        .put("toc", JSONArray().also { array -> toc.forEach { array.put(it.toJson()) } })
        .put("addedAt", addedAt)
        .put("updatedAt", updatedAt)
        .put("lastReadParagraph", lastReadParagraph)

private fun JSONObject.toBook(): Book {
    val paragraphArray = optJSONArray("paragraphs") ?: JSONArray()
    val paragraphs = buildList {
        for (index in 0 until paragraphArray.length()) {
            add(paragraphArray.optString(index))
        }
    }
    val tocArray = optJSONArray("toc") ?: JSONArray()
    val toc = if (paragraphs.isEmpty()) {
        emptyList()
    } else {
        buildList {
            for (index in 0 until tocArray.length()) {
                val item = tocArray.optJSONObject(index) ?: continue
                val title = item.optString("title").trim()
                if (title.isNotBlank()) {
                    add(
                        BookTocEntry(
                            title = title,
                            paragraphIndex = item.optInt("paragraphIndex", 0).coerceIn(0, paragraphs.lastIndex),
                        ),
                    )
                }
            }
        }.distinctBy { it.paragraphIndex to it.title }
    }
    return Book(
        id = getString("id"),
        title = optString("title", "Untitled"),
        fileName = optString("fileName", "unknown"),
        sourceType = optString("sourceType").toEnumOrDefault(SourceType.TXT),
        content = optString("content"),
        paragraphs = paragraphs,
        toc = toc,
        addedAt = optLong("addedAt"),
        updatedAt = optLong("updatedAt"),
        lastReadParagraph = optInt("lastReadParagraph", 0),
    )
}

private fun BookTocEntry.toJson(): JSONObject =
    JSONObject()
        .put("title", title)
        .put("paragraphIndex", paragraphIndex)

private fun Book.isLegacyRawMobiTextImport(): Boolean =
    sourceType == SourceType.TXT &&
        fileName.hasMobiLikeBookExtension() &&
        content.looksLikeRawMobiTextImport()

private fun String.hasMobiLikeBookExtension(): Boolean {
    val extension = substringAfterLast('.', "").lowercase(Locale.ROOT)
    return extension in setOf("mobi", "azw", "azw3", "azw4", "prc")
}

private fun String.looksLikeRawMobiTextImport(): Boolean =
    contains("BOOKMOBI") || contains("EXTH") || contains("kindle:flow")

private fun ReaderNote.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("bookId", bookId)
        .put("bookTitle", bookTitle)
        .put("paragraphIndex", paragraphIndex)
        .put("sentence", sentence)
        .put("translationText", translationText)
        .put("noteText", noteText)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)

private fun JSONObject.toNote(): ReaderNote =
    ReaderNote(
        id = getString("id"),
        bookId = getString("bookId"),
        bookTitle = optString("bookTitle", "Untitled"),
        paragraphIndex = optInt("paragraphIndex"),
        sentence = optString("sentence"),
        translationText = optString("translationText"),
        noteText = optString("noteText"),
        createdAt = optLong("createdAt"),
        updatedAt = optLong("updatedAt"),
    )

private fun LookupHistoryEntry.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("type", type.name)
        .put("bookId", bookId)
        .put("bookTitle", bookTitle)
        .put("paragraphIndex", paragraphIndex)
        .put("sourceText", sourceText)
        .put("resultText", resultText)
        .put("phonetic", phonetic)
        .put("createdAt", createdAt)
        .put("updatedAt", updatedAt)

private fun JSONObject.toLookupHistoryEntry(): LookupHistoryEntry =
    LookupHistoryEntry(
        id = getString("id"),
        type = optString("type").toEnumOrDefault(LookupHistoryType.WORD),
        bookId = optString("bookId"),
        bookTitle = optString("bookTitle", "Untitled"),
        paragraphIndex = optInt("paragraphIndex", 0),
        sourceText = optString("sourceText"),
        resultText = optString("resultText"),
        phonetic = optString("phonetic"),
        createdAt = optLong("createdAt"),
        updatedAt = optLong("updatedAt", optLong("createdAt")),
    )

private fun BookChat.toJson(): JSONObject =
    JSONObject()
        .put("bookId", bookId)
        .put("bookTitle", bookTitle)
        .put("summary", summary)
        .put("messages", JSONArray().also { array -> messages.forEach { array.put(it.toJson()) } })
        .put("updatedAt", updatedAt)

private fun JSONObject.toBookChat(): BookChat {
    val messageArray = optJSONArray("messages") ?: JSONArray()
    val messages = buildList {
        for (index in 0 until messageArray.length()) {
            val item = messageArray.optJSONObject(index) ?: continue
            runCatching { item.toChatMessage() }.getOrNull()?.let { add(it) }
        }
    }
    return BookChat(
        bookId = optString("bookId"),
        bookTitle = optString("bookTitle", "Untitled"),
        summary = optString("summary"),
        messages = messages,
        updatedAt = optLong("updatedAt"),
    )
}

private fun ChatMessage.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("role", role.name)
        .put("content", content)
        .put("createdAt", createdAt)

private fun JSONObject.toChatMessage(): ChatMessage =
    ChatMessage(
        id = optString("id").ifBlank { UUID.randomUUID().toString() },
        role = optString("role").toEnumOrDefault(ChatRole.USER),
        content = optString("content"),
        createdAt = optLong("createdAt"),
    )

private fun LookupHistoryEntry.lookupKey(): String = lookupHistoryKey(type, sourceText)

private fun lookupHistoryKey(type: LookupHistoryType, sourceText: String): String {
    val normalized = sourceText.replace(Regex("\\s+"), " ").trim()
    return when (type) {
        LookupHistoryType.WORD -> normalized.lowercase(Locale.US)
        LookupHistoryType.TRANSLATION -> normalized
    }
}

private fun formatExportTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "未知"
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
