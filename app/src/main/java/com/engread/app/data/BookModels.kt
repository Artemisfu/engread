package com.engread.app.data

data class Book(
    val id: String,
    val title: String,
    val fileName: String,
    val sourceType: SourceType,
    val content: String,
    val paragraphs: List<String>,
    val toc: List<BookTocEntry> = emptyList(),
    val addedAt: Long,
    val updatedAt: Long,
    val lastReadParagraph: Int = 0,
) {
    val progress: Float
        get() {
            if (paragraphs.isEmpty()) return 0f
            if (lastReadParagraph <= 0) return 0f
            return (lastReadParagraph + 1).coerceAtMost(paragraphs.size).toFloat() / paragraphs.size.toFloat()
    }
}

data class BookTocEntry(
    val title: String,
    val paragraphIndex: Int,
)

enum class SourceType {
    TXT,
    MOBI,
    EPUB,
}

data class ReaderSettings(
    val font: ReaderFont = ReaderFont.SANS,
    val noteFont: ReaderFont = ReaderFont.SANS,
    val fontSizeSp: Int = 19,
    val theme: ReaderTheme = ReaderTheme.LIGHT,
    val translation: TranslationSettings = TranslationSettings(),
)

data class TranslationSettings(
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val model: String = "gpt-4o-mini",
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && model.isNotBlank()
}

enum class ReaderFont(val label: String) {
    SANS("无衬线"),
    SERIF("衬线"),
    MONO("等宽"),
    EB_GARAMOND("EB Garamond"),
    LIBRE_BASKERVILLE("Libre Baskerville"),
    MERRIWEATHER("Merriweather"),
    LORA("Lora"),
    CAVEAT("Caveat"),
    KALAM("Kalam"),
    PATRICK_HAND("Patrick Hand"),
    SHADOWS_INTO_LIGHT("Shadows Into Light"),
}

enum class ReaderTheme(val label: String) {
    LIGHT("浅色"),
    PAPER("纸张"),
    DARK("深色"),
}

data class ReaderNote(
    val id: String,
    val bookId: String,
    val bookTitle: String,
    val paragraphIndex: Int,
    val sentence: String,
    val translationText: String = "",
    val noteText: String,
    val noteType: ReaderNoteType = ReaderNoteType.EXCERPT,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class ReaderNoteType(val label: String) {
    EXCERPT("摘句"),
    CHAT("对话"),
}

data class LookupHistoryEntry(
    val id: String,
    val type: LookupHistoryType,
    val bookId: String,
    val bookTitle: String,
    val paragraphIndex: Int = 0,
    val sourceText: String,
    val resultText: String,
    val phonetic: String = "",
    val createdAt: Long,
    val updatedAt: Long = createdAt,
)

enum class LookupHistoryType(val label: String) {
    WORD("查词"),
    TRANSLATION("翻译"),
}

data class BookChat(
    val bookId: String,
    val bookTitle: String,
    val summary: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val updatedAt: Long = 0L,
)

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val createdAt: Long,
)

enum class ChatRole {
    USER,
    ASSISTANT,
}
