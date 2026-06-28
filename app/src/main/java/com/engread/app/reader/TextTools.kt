package com.engread.app.reader

import com.engread.app.data.Book
import com.engread.app.data.ChatMessage
import com.engread.app.data.ChatRole
import com.engread.app.data.TranslationSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.util.Date
import java.util.Locale

data class WordEntry(
    val word: String,
    val phonetic: String,
    val meaning: String,
    val root: String = "",
    val cognates: List<String> = emptyList(),
    val synonyms: List<String> = emptyList(),
    val usPhonetic: String = "",
    val ukPhonetic: String = "",
    val detailsLoading: Boolean = false,
)

data class ReaderPage(
    val index: Int,
    val firstParagraphIndex: Int,
    val lastParagraphIndex: Int,
    val paragraphs: List<ReaderPageParagraph>,
) {
    val text: String
        get() = paragraphs.joinToString("\n\n") { it.text }

    val firstParagraphOffset: Int
        get() = paragraphs.firstOrNull()?.startOffset ?: 0

    fun containsAnchor(paragraphIndex: Int, paragraphOffset: Int): Boolean =
        paragraphs.any { paragraph ->
            paragraph.paragraphIndex == paragraphIndex &&
                paragraphOffset >= paragraph.startOffset &&
                paragraphOffset < paragraph.endOffsetExclusive
        }
}

data class ReaderPageParagraph(
    val paragraphIndex: Int,
    val text: String,
    val isChapterFirstParagraph: Boolean,
    val startOffset: Int = 0,
    val endOffsetExclusive: Int = text.length,
)

data class BookChapter(
    val title: String,
    val paragraphIndex: Int,
)

fun buildReaderPages(
    paragraphs: List<String>,
    maxChars: Int = 760,
    chapterParagraphIndices: Set<Int> = emptySet(),
): List<ReaderPage> {
    val pages = mutableListOf<ReaderPage>()
    val current = mutableListOf<ReaderPageParagraph>()
    var currentChars = 0
    var shouldDropCapNextBody = true

    fun flushPage() {
        if (current.isEmpty()) return
        val pageParagraphs = current.toList()
        pages += ReaderPage(
            index = pages.size,
            firstParagraphIndex = pageParagraphs.first().paragraphIndex,
            lastParagraphIndex = pageParagraphs.last().paragraphIndex,
            paragraphs = pageParagraphs,
        )
        current.clear()
        currentChars = 0
    }

    fun addParagraphChunk(
        paragraphIndex: Int,
        chunk: ParagraphChunk,
        isChapterFirstParagraph: Boolean,
    ) {
        val text = chunk.text
        val estimatedNextChars = currentChars + text.length + if (current.isEmpty()) 0 else 2
        if (current.isNotEmpty() && estimatedNextChars > maxChars) {
            flushPage()
        }
        current += ReaderPageParagraph(
            paragraphIndex = paragraphIndex,
            text = text,
            isChapterFirstParagraph = isChapterFirstParagraph,
            startOffset = chunk.startOffset,
            endOffsetExclusive = chunk.endOffsetExclusive,
        )
        currentChars += text.length + if (current.size == 1) 0 else 2
    }

    paragraphs.forEachIndexed { index, rawParagraph ->
        val paragraph = rawParagraph.trim()
        if (paragraph.isBlank()) return@forEachIndexed
        val isHeading = index in chapterParagraphIndices || isChapterHeading(paragraph)
        val isBody = isBodyParagraph(paragraph)
        if (isHeading && current.isNotEmpty()) {
            flushPage()
        }
        val isChapterFirstParagraph = isBody && shouldDropCapNextBody
        if (isHeading) {
            shouldDropCapNextBody = true
        } else if (isChapterFirstParagraph) {
            shouldDropCapNextBody = false
        }

        val chunks = splitParagraphIntoPageChunks(paragraph, maxChars.coerceAtLeast(420))
        chunks.forEachIndexed { chunkIndex, chunk ->
            addParagraphChunk(
                paragraphIndex = index,
                chunk = chunk,
                isChapterFirstParagraph = isChapterFirstParagraph && chunkIndex == 0,
            )
        }
    }
    flushPage()

    return pages.ifEmpty {
        listOf(
            ReaderPage(
                index = 0,
                firstParagraphIndex = 0,
                lastParagraphIndex = 0,
                paragraphs = listOf(ReaderPageParagraph(0, "这本书暂时没有可阅读内容。", false)),
            ),
        )
    }
}

private data class ParagraphChunk(
    val text: String,
    val startOffset: Int,
    val endOffsetExclusive: Int,
)

private fun splitParagraphIntoPageChunks(paragraph: String, maxChars: Int): List<ParagraphChunk> {
    if (paragraph.length <= maxChars) {
        return listOf(ParagraphChunk(paragraph, 0, paragraph.length))
    }
    val chunks = mutableListOf<String>()
    val chunkRanges = mutableListOf<IntRange>()
    var start = 0
    while (start < paragraph.length) {
        val hardEnd = (start + maxChars).coerceAtMost(paragraph.length)
        if (hardEnd == paragraph.length) {
            val chunk = paragraph.substring(start)
            val bounds = chunk.trimmedBounds(start)
            if (bounds != null) {
                chunks += paragraph.substring(bounds.first, bounds.last + 1)
                chunkRanges += bounds
            }
            break
        }
        val searchStart = (start + (maxChars * 0.55f).toInt()).coerceAtMost(hardEnd)
        val boundary = (hardEnd downTo searchStart).firstOrNull { index ->
            val char = paragraph.getOrNull(index - 1)
            char == '.' || char == '!' || char == '?' || char == ';' || char == ':' || char == '\n'
        } ?: (hardEnd downTo searchStart).firstOrNull { index ->
            paragraph.getOrNull(index - 1)?.isWhitespace() == true
        } ?: hardEnd
        val end = boundary.coerceIn(start + 1, paragraph.length)
        val chunk = paragraph.substring(start, end)
        val bounds = chunk.trimmedBounds(start)
        if (bounds != null) {
            chunks += paragraph.substring(bounds.first, bounds.last + 1)
            chunkRanges += bounds
        }
        start = end
        while (start < paragraph.length && paragraph[start].isWhitespace()) start += 1
    }
    return chunks.zip(chunkRanges)
        .map { (text, range) -> ParagraphChunk(text, range.first, range.last + 1) }
        .filter { it.text.isNotBlank() }
}

private fun String.trimmedBounds(sourceStart: Int): IntRange? {
    val first = indexOfFirst { !it.isWhitespace() }
    if (first < 0) return null
    val last = indexOfLast { !it.isWhitespace() }
    return (sourceStart + first)..(sourceStart + last)
}

fun buildBookChapters(paragraphs: List<String>): List<BookChapter> =
    buildList {
        paragraphs.forEachIndexed { index, rawParagraph ->
            val paragraph = rawParagraph.trim()
            if (paragraph.isNotBlank() && isChapterHeading(paragraph)) {
                add(BookChapter(title = paragraph, paragraphIndex = index))
            }
        }
        if (isEmpty()) {
            val firstTitle = paragraphs.firstOrNull { it.isNotBlank() }?.trim().orEmpty().ifBlank { "开始阅读" }
            add(BookChapter(title = firstTitle, paragraphIndex = 0))
        }
    }

fun chapterDropInitialOffsets(page: ReaderPage): List<Int> {
    val offsets = mutableListOf<Int>()
    var cursor = 0
    page.paragraphs.forEachIndexed { index, paragraph ->
        if (paragraph.isChapterFirstParagraph) {
            val firstLetterIndex = paragraph.text.indexOfFirst { it.isEnglishLetter() }
            if (firstLetterIndex >= 0) offsets += cursor + firstLetterIndex
        }
        cursor += paragraph.text.length
        if (index != page.paragraphs.lastIndex) cursor += 2
    }
    return offsets
}

fun extractWordAt(text: String, offset: Int): String? {
    if (text.isBlank() || offset !in text.indices) return null
    val anchor = when {
        text[offset].isWordChar() -> offset
        offset > 0 && text[offset - 1].isWordChar() -> offset - 1
        else -> return null
    }
    var start = anchor
    var end = anchor
    while (start > 0 && text[start - 1].isWordChar()) start -= 1
    while (end + 1 < text.length && text[end + 1].isWordChar()) end += 1
    return text.substring(start, end + 1).trim('\'', '-').takeIf { it.any(Char::isLetter) }
}

fun splitSentences(paragraph: String): List<String> {
    val normalized = paragraph.replace(Regex("\\s+"), " ").trim()
    if (normalized.isBlank()) return emptyList()
    return normalized
        .split(Regex("(?<=[.!?])\\s+"))
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "未知"
    val formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun Char.isEnglishLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'

private fun Char.isWordChar(): Boolean =
    isEnglishLetter() || this == '\'' || this == '-'

private fun isBodyParagraph(paragraph: String): Boolean {
    val normalized = paragraph.trim()
    if (normalized.length < 32) return false
    if (isChapterHeading(normalized)) return false
    return normalized.count { it.isLetter() } >= 20
}

private fun isChapterHeading(paragraph: String): Boolean {
    val normalized = paragraph.trim()
    if (normalized.length !in 3..90) return false
    val chapterPattern = Regex(
        pattern = "^(chapter|book|part|volume)\\s+([ivxlcdm0-9]+|one|two|three|four|five|six|seven|eight|nine|ten).*$|^(prologue|epilogue|preface|introduction)$|^第\\s*[一二三四五六七八九十百千万0-9]+\\s*[章节卷部].*",
        option = RegexOption.IGNORE_CASE,
    )
    return chapterPattern.matches(normalized)
}

object LocalDictionary {
    private val entries = listOf(
        WordEntry("about", "/əˈbaʊt/", "关于；大约；在附近"),
        WordEntry("after", "/ˈæftər/", "在……之后；后来"),
        WordEntry("again", "/əˈɡen/", "再次；又一次"),
        WordEntry("air", "/er/", "空气；神态；旋律"),
        WordEntry("always", "/ˈɔːlweɪz/", "总是；一直"),
        WordEntry("among", "/əˈmʌŋ/", "在……之中"),
        WordEntry("answer", "/ˈænsər/", "回答；答案"),
        WordEntry("because", "/bɪˈkɔːz/", "因为"),
        WordEntry("before", "/bɪˈfɔːr/", "在……之前"),
        WordEntry("between", "/bɪˈtwiːn/", "在两者之间"),
        WordEntry("book", "/bʊk/", "书；预订"),
        WordEntry("child", "/tʃaɪld/", "儿童；孩子"),
        WordEntry("city", "/ˈsɪti/", "城市"),
        WordEntry("day", "/deɪ/", "一天；白天"),
        WordEntry("door", "/dɔːr/", "门"),
        WordEntry("dream", "/driːm/", "梦；梦想"),
        WordEntry("earth", "/ɜːrθ/", "地球；土地"),
        WordEntry("even", "/ˈiːvən/", "甚至；平坦的"),
        WordEntry("eye", "/aɪ/", "眼睛"),
        WordEntry("face", "/feɪs/", "脸；面对"),
        WordEntry("family", "/ˈfæməli/", "家庭；家人"),
        WordEntry("friend", "/frend/", "朋友"),
        WordEntry("good", "/ɡʊd/", "好的；善良的"),
        WordEntry("great", "/ɡreɪt/", "伟大的；很大的"),
        WordEntry("hand", "/hænd/", "手；递给"),
        WordEntry("heart", "/hɑːrt/", "心；内心"),
        WordEntry("home", "/hoʊm/", "家；回家"),
        WordEntry("house", "/haʊs/", "房子"),
        WordEntry("know", "/noʊ/", "知道；了解"),
        WordEntry("life", "/laɪf/", "生命；生活"),
        WordEntry("light", "/laɪt/", "光；轻的；点燃"),
        WordEntry("little", "/ˈlɪtəl/", "小的；少量"),
        WordEntry("look", "/lʊk/", "看；样子"),
        WordEntry("love", "/lʌv/", "爱；喜爱"),
        WordEntry("man", "/mæn/", "男人；人类"),
        WordEntry("mind", "/maɪnd/", "思想；介意"),
        WordEntry("morning", "/ˈmɔːrnɪŋ/", "早晨"),
        WordEntry("mother", "/ˈmʌðər/", "母亲"),
        WordEntry("night", "/naɪt/", "夜晚"),
        WordEntry("people", "/ˈpiːpəl/", "人们"),
        WordEntry("place", "/pleɪs/", "地方；放置"),
        WordEntry("read", "/riːd/", "阅读；读"),
        WordEntry("room", "/ruːm/", "房间；空间"),
        WordEntry("said", "/sed/", "说；表示"),
        WordEntry("school", "/skuːl/", "学校"),
        WordEntry("story", "/ˈstɔːri/", "故事"),
        WordEntry("thing", "/θɪŋ/", "事情；东西"),
        WordEntry("thought", "/θɔːt/", "想法；思考的过去式"),
        WordEntry("time", "/taɪm/", "时间；次数"),
        WordEntry("water", "/ˈwɔːtər/", "水"),
        WordEntry("way", "/weɪ/", "道路；方式"),
        WordEntry("world", "/wɜːrld/", "世界"),
        WordEntry("young", "/jʌŋ/", "年轻的"),
    ).associateBy { it.word }

    fun lookup(word: String): WordEntry =
        entries[word.lowercase(Locale.US)] ?: WordEntry(
            word = word,
            phonetic = "未收录",
            meaning = "本地词典暂无释义。后续可以接入有道、欧路、牛津等词典服务。",
        )
}

object OpenAiChatTranslator {
    suspend fun translate(text: String, settings: TranslationSettings): String =
        withContext(Dispatchers.IO) {
            if (!settings.isConfigured) {
                error("请先在阅读设置里填写 Base URL、API Key 和模型。")
            }
            settings.createChatCompletion(
                messages = JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put(
                                "content",
                                "You are a careful English-to-Simplified-Chinese literary translator. " +
                                    "Keep names and terms accurate, preserve meaning and tone, and output only the Chinese translation.",
                            ),
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", "Translate this selected English text into natural Simplified Chinese:\n\n$text"),
                    ),
                temperature = 0.2,
            )
        }
}

object OpenAiWordLookup {
    suspend fun lookup(word: String, context: String, settings: TranslationSettings): WordEntry =
        withContext(Dispatchers.IO) {
            if (!settings.isConfigured) {
                error("请先在阅读设置里填写 Base URL、API Key 和模型。")
            }
            val content = settings.createChatCompletion(
                messages = JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put(
                                    "content",
                                    "You are an English dictionary API for Chinese readers. Return only valid minified JSON. " +
                                    "The JSON schema is exactly: {\"word\":\"string\",\"phonetic\":\"string\",\"usPhonetic\":\"string\",\"ukPhonetic\":\"string\",\"meaning\":\"string\",\"root\":\"string\",\"cognates\":[\"string\"],\"synonyms\":[\"string\"]}. " +
                                    "phonetic, usPhonetic, and ukPhonetic must be IPA wrapped in slashes when known. phonetic may duplicate usPhonetic when only one IPA is known. meaning must be concise Simplified Chinese definitions, " +
                                    "separated with Chinese semicolons. root must explain useful roots/prefixes/suffixes in Simplified Chinese. " +
                                    "cognates must list 3-6 English same-root or related-family words when useful. synonyms must list 3-6 English synonyms. " +
                                    "Use empty string or empty arrays when not useful. Do not return markdown or extra keys.",
                            ),
                    )
                    .put(
                        JSONObject()
                            .put(
                                "role",
                                "user",
                            )
                            .put("content", "Word: $word\nContext: $context"),
                    ),
                temperature = 0.1,
            )
            val root = JSONObject(content.extractJsonObjectText())
            WordEntry(
                word = root.optString("word", word).ifBlank { word },
                phonetic = root.optString("phonetic", "未知").ifBlank { "未知" },
                meaning = root.optString("meaning", "暂无释义").ifBlank { "暂无释义" },
                root = root.optString("root", "").trim(),
                cognates = root.optStringList("cognates"),
                synonyms = root.optStringList("synonyms"),
                usPhonetic = root.optString("usPhonetic", "").trim(),
                ukPhonetic = root.optString("ukPhonetic", "").trim(),
            )
        }
}

object OpenAiBookChat {
    suspend fun reply(
        book: Book,
        summary: String,
        recentMessages: List<ChatMessage>,
        settings: TranslationSettings,
    ): String =
        withContext(Dispatchers.IO) {
            if (!settings.isConfigured) {
                error("请先在设置里填写 Base URL、API Key 和模型。")
            }
            val currentUserMessage = recentMessages.lastOrNull { it.role == ChatRole.USER }?.content.orEmpty()
            val latestFragments = recentMessages.dropLast(1).takeLast(10).toPromptTranscript().ifBlank { "暂无。" }
            val userPrompt = buildBookChatPrompt(book, summary, latestFragments, currentUserMessage)
            runCatching {
                settings.createBookToolLoopCompletion(book, userPrompt)
            }.getOrElse {
                settings.createChatCompletion(
                    messages = bookChatMessages(userPrompt),
                    temperature = 0.35,
                )
            }
        }

    suspend fun suggestQuestions(
        book: Book,
        summary: String,
        recentMessages: List<ChatMessage>,
        settings: TranslationSettings,
    ): List<String> =
        withContext(Dispatchers.IO) {
            if (!settings.isConfigured) {
                error("请先在设置里填写 Base URL、API Key 和模型。")
            }
            val currentUserMessage = recentMessages.lastOrNull { it.role == ChatRole.USER }?.content.orEmpty()
            val latestFragments = recentMessages.takeLast(10).toPromptTranscript().ifBlank { "暂无。" }
            val prompt = buildBookChatPrompt(book, summary, latestFragments, currentUserMessage) +
                "\n\n请根据上面的最新阅读讨论，生成 3 个用户下一步可能想问的问题。" +
                "要求：只返回 JSON 字符串数组；每个问题 8 到 24 个汉字；问题要具体，能继续推进阅读理解。"
            val content = settings.createChatCompletion(
                messages = JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put("content", "You generate concise follow-up reading questions. Return JSON array only."),
                    )
                    .put(JSONObject().put("role", "user").put("content", prompt)),
                temperature = 0.55,
            )
            content.toQuestionList().take(3)
        }

    suspend fun summarize(
        book: Book,
        previousSummary: String,
        newMessages: List<ChatMessage>,
        settings: TranslationSettings,
    ): String =
        withContext(Dispatchers.IO) {
            if (!settings.isConfigured) {
                error("请先在设置里填写 Base URL、API Key 和模型。")
            }
            settings.createChatCompletion(
                messages = JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put(
                                "content",
                                "You maintain compact long-term memory for a book chat. Output concise Simplified Chinese markdown only. " +
                                    "Keep: history topic list and conclusions, current topic, each topic's user need, solution, key facts, external links, and citations/quotes if any. " +
                                    "Update the old summary with the new user/assistant turn. Preserve useful prior facts and remove filler.",
                            ),
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put(
                                "content",
                                buildString {
                                    appendLine("Book title: ${book.title}")
                                    appendLine("Previous compressed memory:")
                                    appendLine(previousSummary.ifBlank { "暂无。"})
                                    appendLine()
                                    appendLine("New raw messages:")
                                    appendLine(newMessages.toPromptTranscript())
                                    appendLine()
                                    appendLine("Return the updated compressed memory.")
                                },
                            ),
                    ),
                temperature = 0.15,
            )
        }
}

private fun buildBookChatPrompt(
    book: Book,
    summary: String,
    latestFragments: String,
    currentUserMessage: String,
): String =
    buildString {
        appendLine("目前针对数据${book.toDiscussionDataPrompt()}进行讨论，")
        appendLine("历史对话摘要：${summary.ifBlank { "暂无。" }}")
        appendLine("最近话题：${summary.currentTopicForPrompt()}")
        appendLine("最新对话片段：$latestFragments")
        appendLine("当前用户说：$currentUserMessage")
    }

private fun bookChatMessages(userPrompt: String): JSONArray =
    JSONArray()
        .put(
            JSONObject()
                .put("role", "system")
                .put(
                    "content",
                    "You are EngRead's warm reading companion for Chinese readers of English books. " +
                        "Answer in Simplified Chinese with a gentle, practical tone. Use the compressed memory and recent turns. " +
                        "Use Markdown where it improves readability. When useful, call the book search tools before answering. " +
                        "If you are unsure, say so briefly.",
                ),
        )
        .put(JSONObject().put("role", "user").put("content", userPrompt))

private fun TranslationSettings.createBookToolLoopCompletion(book: Book, userPrompt: String): String {
    val messages = bookChatMessages(userPrompt)
    val tools = book.bookChatToolDefinitions()
    var lastContent = ""
    repeat(4) {
        val message = createChatCompletionMessage(
            messages = messages,
            temperature = 0.35,
            tools = tools,
        )
        lastContent = message.optString("content").trim()
        val toolCalls = message.optJSONArray("tool_calls")
        if (toolCalls == null || toolCalls.length() == 0) {
            return lastContent.takeIf { it.isNotBlank() } ?: error("服务没有返回可用内容。")
        }
        messages.put(message)
        for (index in 0 until toolCalls.length()) {
            val call = toolCalls.optJSONObject(index) ?: continue
            val function = call.optJSONObject("function") ?: continue
            val name = function.optString("name")
            val args = function.optString("arguments").toJsonObjectOrEmpty()
            val result = runCatching { book.executeBookTool(name, args) }
                .getOrElse { error -> JSONObject().put("error", error.message ?: "tool failed") }
            messages.put(
                JSONObject()
                    .put("role", "tool")
                    .put("tool_call_id", call.optString("id"))
                    .put("content", result.toString()),
            )
        }
    }
    return lastContent.takeIf { it.isNotBlank() } ?: error("工具调用没有产生最终回复。")
}

private fun Book.bookChatToolDefinitions(): JSONArray =
    JSONArray()
        .put(
            JSONObject()
                .put("type", "function")
                .put(
                    "function",
                    JSONObject()
                        .put("name", "search_book")
                        .put("description", "Search this book by keyword and return paragraph positions with short snippets.")
                        .put(
                            "parameters",
                            JSONObject()
                                .put("type", "object")
                                .put(
                                    "properties",
                                    JSONObject()
                                        .put("query", JSONObject().put("type", "string").put("description", "Keyword or phrase to search."))
                                        .put(
                                            "max_results",
                                            JSONObject()
                                                .put("type", "integer")
                                                .put("description", "Maximum result count, 1-10.")
                                                .put("minimum", 1)
                                                .put("maximum", 10),
                                        ),
                                )
                                .put("required", JSONArray().put("query")),
                        ),
                ),
        )
        .put(
            JSONObject()
                .put("type", "function")
                .put(
                    "function",
                    JSONObject()
                        .put("name", "get_text_block")
                        .put("description", "Fetch a text block around a paragraph position in this book.")
                        .put(
                            "parameters",
                            JSONObject()
                                .put("type", "object")
                                .put(
                                    "properties",
                                    JSONObject()
                                        .put(
                                            "paragraph_index",
                                            JSONObject()
                                                .put("type", "integer")
                                                .put("description", "Zero-based paragraph index returned by search_book."),
                                        )
                                        .put("before", JSONObject().put("type", "integer").put("minimum", 0).put("maximum", 4))
                                        .put("after", JSONObject().put("type", "integer").put("minimum", 0).put("maximum", 6)),
                                )
                                .put("required", JSONArray().put("paragraph_index")),
                        ),
                ),
        )
        .put(
            JSONObject()
                .put("type", "function")
                .put(
                    "function",
                    JSONObject()
                        .put("name", "get_table_of_contents")
                        .put("description", "Return this book's table of contents entries with paragraph positions.")
                        .put(
                            "parameters",
                            JSONObject()
                                .put("type", "object")
                                .put("properties", JSONObject()),
                        ),
                ),
        )

private fun Book.executeBookTool(name: String, args: JSONObject): JSONObject =
    when (name) {
        "search_book" -> searchBookTool(args.optString("query"), args.optInt("max_results", 6))
        "get_text_block" -> getTextBlockTool(args.optInt("paragraph_index"), args.optInt("before", 1), args.optInt("after", 2))
        "get_table_of_contents" -> JSONObject()
            .put("title", title)
            .put(
                "toc",
                JSONArray().also { array ->
                    toc.take(80).forEach { entry ->
                        array.put(
                            JSONObject()
                                .put("title", entry.title)
                                .put("paragraph_index", entry.paragraphIndex),
                        )
                    }
                },
            )
        else -> JSONObject().put("error", "Unknown tool: $name")
    }

private fun Book.searchBookTool(query: String, maxResults: Int): JSONObject {
    val normalizedQuery = query.replace(Regex("\\s+"), " ").trim()
    val queryLower = normalizedQuery.lowercase(Locale.ROOT)
    val tokens = queryLower.split(Regex("\\s+")).filter { it.length >= 2 }.distinct()
    if (queryLower.isBlank()) return JSONObject().put("results", JSONArray())
    val results = paragraphs
        .mapIndexedNotNull { index, paragraph ->
            val lower = paragraph.lowercase(Locale.ROOT)
            val exactIndex = lower.indexOf(queryLower)
            val tokenHits = tokens.count { it in lower }
            val score = when {
                exactIndex >= 0 -> 100 + tokenHits
                tokenHits > 0 -> tokenHits
                else -> 0
            }
            if (score <= 0) null else Triple(index, score, exactIndex.coerceAtLeast(0))
        }
        .sortedWith(compareByDescending<Triple<Int, Int, Int>> { it.second }.thenBy { it.first })
        .take(maxResults.coerceIn(1, 10))
    return JSONObject()
        .put("query", normalizedQuery)
        .put(
            "results",
            JSONArray().also { array ->
                results.forEach { (index, _, matchIndex) ->
                    array.put(
                        JSONObject()
                            .put("paragraph_index", index)
                            .put("chapter", chapterTitleForParagraph(index))
                            .put("snippet", paragraphs[index].snippetAround(matchIndex, 260)),
                    )
                }
            },
        )
}

private fun Book.getTextBlockTool(paragraphIndex: Int, before: Int, after: Int): JSONObject {
    val center = paragraphIndex.coerceIn(0, (paragraphs.size - 1).coerceAtLeast(0))
    val start = (center - before.coerceIn(0, 4)).coerceAtLeast(0)
    val end = (center + after.coerceIn(0, 6)).coerceAtMost((paragraphs.size - 1).coerceAtLeast(0))
    return JSONObject()
        .put("paragraph_index", center)
        .put("chapter", chapterTitleForParagraph(center))
        .put(
            "blocks",
            JSONArray().also { array ->
                for (index in start..end) {
                    array.put(
                        JSONObject()
                            .put("paragraph_index", index)
                            .put("text", paragraphs.getOrNull(index).orEmpty()),
                    )
                }
            },
        )
}

private fun Book.chapterTitleForParagraph(paragraphIndex: Int): String =
    toc.lastOrNull { it.paragraphIndex <= paragraphIndex }?.title ?: title

private fun String.snippetAround(matchIndex: Int, maxLength: Int): String {
    val normalized = replace(Regex("\\s+"), " ").trim()
    if (normalized.length <= maxLength) return normalized
    val start = (matchIndex - maxLength / 3).coerceIn(0, (normalized.length - maxLength).coerceAtLeast(0))
    return buildString {
        if (start > 0) append("...")
        append(normalized.substring(start, (start + maxLength).coerceAtMost(normalized.length)).trim())
        if (start + maxLength < normalized.length) append("...")
    }
}

private fun List<ChatMessage>.toPromptTranscript(): String =
    joinToString("\n") { message ->
        val role = when (message.role) {
            ChatRole.USER -> "User"
            ChatRole.ASSISTANT -> "Assistant"
        }
        "$role: ${message.content}"
    }

private fun Book.toDiscussionDataPrompt(): String =
    buildString {
        append("《").append(title).append("》")
        append("（文件：").append(fileName.ifBlank { "未知" })
        append("；类型：").append(sourceType.name)
        append("；段落数：").append(paragraphs.size)
        append("）")
        val currentParagraph = paragraphs.getOrNull(lastReadParagraph)?.compactPromptText(240)
        if (!currentParagraph.isNullOrBlank()) {
            append("\n当前阅读附近文本：").append(currentParagraph)
        }
        if (toc.isNotEmpty()) {
            append("\n目录片段：")
            append(toc.take(12).joinToString("；") { it.title })
        }
    }

private fun String.currentTopicForPrompt(): String =
    lineSequence()
        .firstOrNull { line -> line.contains("当前话题") || line.contains("最近话题") }
        ?.let { line -> line.substringAfter("：", line).substringAfter(":", line).trim() }
        ?.takeIf { it.isNotBlank() }
        ?: "暂无明确最近话题"

private fun String.compactPromptText(maxLength: Int): String {
    val normalized = replace(Regex("\\s+"), " ").trim()
    return if (normalized.length <= maxLength) normalized else normalized.take(maxLength).trimEnd() + "..."
}

private fun String.toQuestionList(): List<String> {
    val arrayText = extractJsonArrayText()
    val array = JSONArray(arrayText)
    return buildList {
        for (index in 0 until array.length()) {
            array.optString(index).trim().takeIf { it.isNotBlank() }?.let { add(it) }
        }
    }
}

private fun JSONObject.optStringList(name: String): List<String> {
    val array = optJSONArray(name) ?: return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            array.optString(index).trim().takeIf { it.isNotBlank() }?.let { add(it) }
        }
    }
}

private fun TranslationSettings.createChatCompletion(messages: JSONArray, temperature: Double): String =
    createChatCompletionMessage(messages = messages, temperature = temperature)
        .optString("content")
        .trim()
        .takeIf { it.isNotBlank() }
        ?: error("服务没有返回可用内容。")

private fun TranslationSettings.createChatCompletionMessage(
    messages: JSONArray,
    temperature: Double,
    tools: JSONArray? = null,
): JSONObject {
    val endpoint = baseUrl.toChatCompletionsEndpoint()
    val body = JSONObject()
        .put("model", model.trim())
        .put("messages", messages)
        .put("temperature", temperature)
    if (tools != null && tools.length() > 0) {
        body.put("tools", tools)
        body.put("tool_choice", "auto")
    }

    val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 20_000
        readTimeout = 45_000
        doOutput = true
        setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
        setRequestProperty("Content-Type", "application/json")
        setRequestProperty("Accept", "application/json")
    }

    try {
        val bytes = body.toString().toByteArray(StandardCharsets.UTF_8)
        connection.outputStream.use { output -> output.write(bytes) }

        val responseCode = connection.responseCode
        val responseText = if (responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            (connection.errorStream ?: connection.inputStream).bufferedReader().use { it.readText() }
        }

        if (responseCode !in 200..299) {
            error(responseText.toApiErrorMessage(responseCode))
        }

        return JSONObject(responseText)
            .optJSONArray("choices")
            ?.optJSONObject(0)
            ?.optJSONObject("message")
            ?.takeIf { message ->
                message.optString("content").isNotBlank() ||
                    (message.optJSONArray("tool_calls")?.length() ?: 0) > 0
            }
            ?: error("服务没有返回可用内容。")
    } finally {
        connection.disconnect()
    }
}

private fun String.toChatCompletionsEndpoint(): String {
    val trimmed = trim().trimEnd('/')
    return when {
        trimmed.endsWith("/chat/completions") -> trimmed
        trimmed == "https://api.openai.com" -> "$trimmed/v1/chat/completions"
        trimmed == "http://api.openai.com" -> "$trimmed/v1/chat/completions"
        else -> "$trimmed/chat/completions"
    }
}

private fun String.extractJsonObjectText(): String {
    val trimmed = trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
    val start = trimmed.indexOf('{')
    val end = trimmed.lastIndexOf('}')
    if (start < 0 || end <= start) error("查词服务没有返回 JSON。")
    return trimmed.substring(start, end + 1)
}

private fun String.extractJsonArrayText(): String {
    val trimmed = trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
    val start = trimmed.indexOf('[')
    val end = trimmed.lastIndexOf(']')
    if (start >= 0 && end > start) return trimmed.substring(start, end + 1)
    val fallback = lineSequence()
        .map { it.trim().trimStart('-', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '、') }
        .filter { it.isNotBlank() }
        .take(3)
        .toList()
    return JSONArray(fallback).toString()
}

private fun String.toJsonObjectOrEmpty(): JSONObject =
    runCatching { JSONObject(ifBlank { "{}" }) }.getOrElse { JSONObject() }

private fun String.toApiErrorMessage(responseCode: Int): String {
    val apiMessage = runCatching {
        JSONObject(this).optJSONObject("error")?.optString("message")
    }.getOrNull()
    return if (apiMessage.isNullOrBlank()) {
        "翻译失败：HTTP $responseCode"
    } else {
        "翻译失败：$apiMessage"
    }
}
