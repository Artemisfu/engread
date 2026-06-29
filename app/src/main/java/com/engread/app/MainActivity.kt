package com.engread.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.media.AudioAttributes
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChangedIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.engread.app.data.Book
import com.engread.app.data.BookChat
import com.engread.app.data.ChatMessage
import com.engread.app.data.ChatRole
import com.engread.app.data.LibraryRepository
import com.engread.app.data.LookupHistoryEntry
import com.engread.app.data.LookupHistoryType
import com.engread.app.data.ReaderFont
import com.engread.app.data.ReaderNote
import com.engread.app.data.ReaderNoteType
import com.engread.app.data.ReaderSettings
import com.engread.app.data.ReaderTheme
import com.engread.app.reader.BookChapter
import com.engread.app.reader.EcdictDictionary
import com.engread.app.reader.OpenAiBookChat
import com.engread.app.reader.OpenAiChatTranslator
import com.engread.app.reader.OpenAiWordLookup
import com.engread.app.reader.ReaderPage
import com.engread.app.reader.WordEntry
import com.engread.app.reader.buildBookChapters
import com.engread.app.reader.buildReaderPages
import com.engread.app.reader.chapterDropInitialOffsets
import com.engread.app.reader.extractWordAt
import com.engread.app.reader.formatTimestamp
import com.engread.app.ui.EngReadTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlInline
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text as MarkdownTextNode
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.random.Random

private object ReaderVolumeKeyPager {
    var previous: (() -> Unit)? = null
    var next: (() -> Unit)? = null

    fun clear(previousHandler: () -> Unit, nextHandler: () -> Unit) {
        if (previous === previousHandler) previous = null
        if (next === nextHandler) next = null
    }
}

private val defaultReadingQuestions = listOf(
    "这一章的核心问题是什么？",
    "作者在这里想说服我什么？",
    "这段话和前文有什么联系？",
    "这里有没有隐藏的转折？",
    "这一段可以怎么概括？",
    "哪些词句最值得记住？",
    "这本书目前的主题线索是什么？",
    "我应该带着什么问题继续读？",
    "这个人物的动机是什么？",
    "这处描写有什么象征意义？",
    "这一节有哪些关键事实？",
    "能帮我做三条阅读笔记吗？",
    "这段英文有什么难句结构？",
    "哪些表达适合摘抄仿写？",
    "这个章节和书名有什么关系？",
    "作者的论证哪里最强？",
    "作者的论证哪里可能薄弱？",
    "下一页我该重点留意什么？",
    "能用简单中文解释这段吗？",
    "这一章可以怎样复述？",
)

private val selectionChatQuestionPrompts = listOf(
    "这段话的核心意思是什么？",
    "这段英文有什么难句结构？",
    "这段话可以怎么翻译得更自然？",
    "这段话里哪些词值得记住？",
    "这段话和前文有什么联系？",
    "这段话表达了什么情绪或立场？",
    "请用简单英文改写这段话。",
    "这段话有什么文化或背景信息？",
    "这段话适合做什么摘句笔记？",
    "我应该怎样理解这句话的隐含意思？",
)

private fun List<String>.randomThree(): List<String> =
    shuffled(Random(System.currentTimeMillis())).take(3)

private fun ReaderFont.toFontFamily(): FontFamily =
    when (this) {
        ReaderFont.SANS -> FontFamily.SansSerif
        ReaderFont.SERIF -> FontFamily.Serif
        ReaderFont.MONO -> FontFamily.Monospace
        ReaderFont.EB_GARAMOND -> FontFamily(Font(R.font.eb_garamond))
        ReaderFont.LIBRE_BASKERVILLE -> FontFamily(Font(R.font.libre_baskerville))
        ReaderFont.MERRIWEATHER -> FontFamily(Font(R.font.merriweather))
        ReaderFont.LORA -> FontFamily(Font(R.font.lora))
        ReaderFont.CAVEAT -> FontFamily(Font(R.font.caveat))
        ReaderFont.KALAM -> FontFamily(Font(R.font.kalam))
        ReaderFont.PATRICK_HAND -> FontFamily(Font(R.font.patrick_hand))
        ReaderFont.SHADOWS_INTO_LIGHT -> FontFamily(Font(R.font.shadows_into_light))
    }

private val ReaderFont.categoryLabel: String
    get() = when (this) {
        ReaderFont.SANS,
        ReaderFont.SERIF,
        ReaderFont.MONO -> "系统"
        ReaderFont.EB_GARAMOND,
        ReaderFont.LIBRE_BASKERVILLE,
        ReaderFont.MERRIWEATHER,
        ReaderFont.LORA -> "印刷"
        ReaderFont.CAVEAT,
        ReaderFont.KALAM,
        ReaderFont.PATRICK_HAND,
        ReaderFont.SHADOWS_INTO_LIGHT -> "手写"
    }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngReadApp()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val isVolumeKey = event.keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        val hasReaderHandler = ReaderVolumeKeyPager.previous != null || ReaderVolumeKeyPager.next != null
        if (isVolumeKey && hasReaderHandler) {
            if (event.action == KeyEvent.ACTION_UP) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> ReaderVolumeKeyPager.previous?.invoke()
                    KeyEvent.KEYCODE_VOLUME_DOWN -> ReaderVolumeKeyPager.next?.invoke()
                }
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}

private sealed class AppScreen {
    object Shelf : AppScreen()
    object Notes : AppScreen()
    object Chat : AppScreen()
    object Settings : AppScreen()
    data class Reader(val bookId: String) : AppScreen()
}

private val AppScreenSaver = Saver<AppScreen, String>(
    save = { screen ->
        when (screen) {
            AppScreen.Shelf -> "shelf"
            AppScreen.Notes -> "notes"
            AppScreen.Chat -> "chat"
            AppScreen.Settings -> "settings"
            is AppScreen.Reader -> "reader:${screen.bookId}"
        }
    },
    restore = { value ->
        when {
            value == "notes" -> AppScreen.Notes
            value == "chat" -> AppScreen.Chat
            value == "settings" -> AppScreen.Settings
            value.startsWith("reader:") && value.length > "reader:".length -> {
                AppScreen.Reader(value.substringAfter("reader:"))
            }
            else -> AppScreen.Shelf
        }
    },
)

private data class UndoNotice(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val actionLabel: String = "恢复",
    val onAction: suspend () -> Unit,
)

@Composable
private fun EngReadApp() {
    val context = LocalContext.current
    val repository = remember { LibraryRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var screen by rememberSaveable(stateSaver = AppScreenSaver) {
        mutableStateOf<AppScreen>(AppScreen.Shelf)
    }
    var books by remember { mutableStateOf(repository.getBooks()) }
    var notes by remember { mutableStateOf(repository.getNotes()) }
    var lookupHistory by remember { mutableStateOf(repository.getLookupHistory()) }
    var bookChats by remember { mutableStateOf(repository.getBookChats()) }
    var settings by remember { mutableStateOf(repository.getSettings()) }
    var sendingChatBookId by remember { mutableStateOf<String?>(null) }
    var suggestingChatBookId by remember { mutableStateOf<String?>(null) }
    var preferredChatBookId by rememberSaveable { mutableStateOf("") }
    var chatSuggestionsByBook by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var undoNotices by remember { mutableStateOf<List<UndoNotice>>(emptyList()) }
    var lastHomeBackAt by remember { mutableStateOf(0L) }

    fun refreshAll() {
        books = repository.getBooks()
        notes = repository.getNotes()
        lookupHistory = repository.getLookupHistory()
        bookChats = repository.getBookChats()
        settings = repository.getSettings()
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    fun showUndoMessage(message: String, onRestore: suspend () -> Unit) {
        undoNotices = undoNotices + UndoNotice(message = message, onAction = onRestore)
    }

    fun dismissUndoNotice(id: String) {
        undoNotices = undoNotices.filterNot { it.id == id }
    }

    fun openReader(book: Book, paragraphIndex: Int?) {
        val targetParagraph = paragraphIndex?.coerceIn(0, (book.paragraphs.size - 1).coerceAtLeast(0))
        if (targetParagraph != null) {
            val now = System.currentTimeMillis()
            books = books.map { item ->
                if (item.id == book.id) {
                    item.copy(lastReadParagraph = targetParagraph, updatedAt = now)
                } else {
                    item
                }
            }
            scope.launch(Dispatchers.IO) {
                repository.updateProgress(book.id, targetParagraph)
            }
        }
        screen = AppScreen.Reader(book.id)
    }

    BackHandler {
        when (screen) {
            is AppScreen.Reader -> {
                refreshAll()
                screen = AppScreen.Shelf
            }

            AppScreen.Shelf -> {
                val now = System.currentTimeMillis()
                if (now - lastHomeBackAt < 1_800L) {
                    (context as? Activity)?.moveTaskToBack(true)
                } else {
                    lastHomeBackAt = now
                    showMessage("再次侧拉返回桌面")
                }
            }

            AppScreen.Notes,
            AppScreen.Chat,
            AppScreen.Settings -> screen = AppScreen.Shelf
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { repository.importBook(uri) }
            }
            result.onSuccess { book ->
                refreshAll()
                screen = AppScreen.Reader(book.id)
                showMessage("已导入《${book.title}》")
            }.onFailure { error ->
                showMessage(error.message ?: "导入失败")
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val markdown = repository.buildNotesMarkdown()
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                        writer.write(markdown)
                    } ?: error("无法写入导出文件")
                }
            }
            result.onSuccess { showMessage("笔记已导出") }
                .onFailure { showMessage(it.message ?: "导出失败") }
        }
    }

    fun requestChatSuggestions(book: Book) {
        if (!settings.translation.isConfigured) {
            chatSuggestionsByBook = chatSuggestionsByBook + (book.id to defaultReadingQuestions.randomThree())
            return
        }
        scope.launch {
            suggestingChatBookId = book.id
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val chat = repository.getBookChat(book.id)
                    OpenAiBookChat.suggestQuestions(
                        book = book,
                        summary = chat?.summary.orEmpty(),
                        recentMessages = chat?.messages.orEmpty().takeLast(10),
                        settings = settings.translation,
                    )
                }
            }
            suggestingChatBookId = null
            result.onSuccess { questions ->
                chatSuggestionsByBook = chatSuggestionsByBook + (book.id to questions.ifEmpty { defaultReadingQuestions.randomThree() })
            }.onFailure {
                chatSuggestionsByBook = chatSuggestionsByBook + (book.id to defaultReadingQuestions.randomThree())
            }
        }
    }

    fun sendBookChatMessage(
        book: Book,
        text: String,
        quotedParagraph: String = "",
        quotedParagraphIndex: Int = book.lastReadParagraph,
    ) {
        val content = text.trim()
        if (content.isBlank()) return
        if (!settings.translation.isConfigured) {
            showMessage("请先在设置里填写 Base URL、API Key 和模型")
            return
        }
        scope.launch {
            sendingChatBookId = book.id
            val userMessageResult = withContext(Dispatchers.IO) {
                runCatching {
                    val currentChat = repository.getBookChat(book.id)
                    val existingMessages = currentChat?.messages.orEmpty()
                    val userContent = if (quotedParagraph.isBlank()) {
                        content
                    } else {
                        buildString {
                            appendLine("针对这段原文：")
                            appendLine("> ${quotedParagraph.replace("\n", "\n> ")}")
                            appendLine()
                            append("我的问题：").append(content)
                        }
                    }
                    val userMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = ChatRole.USER,
                        content = userContent,
                        createdAt = System.currentTimeMillis(),
                    )
                    repository.saveBookChat(
                        book = book,
                        summary = currentChat?.summary.orEmpty(),
                        messages = existingMessages + userMessage,
                    )
                    userMessage
                }
            }
            val userMessage = userMessageResult.getOrElse { error ->
                sendingChatBookId = null
                showMessage(error.message ?: "发送失败")
                return@launch
            }
            refreshAll()

            val answerResult = withContext(Dispatchers.IO) {
                runCatching {
                    val currentChat = repository.getBookChat(book.id)
                    val messagesWithUser = currentChat?.messages.orEmpty()
                    val recentMessages = messagesWithUser.takeLast(10)
                    val answer = OpenAiBookChat.reply(
                        book = book,
                        summary = currentChat?.summary.orEmpty(),
                        recentMessages = recentMessages,
                        settings = settings.translation,
                    )
                    val assistantMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        role = ChatRole.ASSISTANT,
                        content = answer,
                        createdAt = System.currentTimeMillis(),
                    )
                    val newMessages = listOf(userMessage, assistantMessage)
                    val nextSummary = runCatching {
                        OpenAiBookChat.summarize(
                            book = book,
                            previousSummary = currentChat?.summary.orEmpty(),
                            newMessages = newMessages,
                            settings = settings.translation,
                        )
                    }.getOrElse { currentChat?.summary.orEmpty() }
                    repository.saveBookChat(
                        book = book,
                        summary = nextSummary,
                        messages = messagesWithUser + assistantMessage,
                    )
                    if (quotedParagraph.isNotBlank()) {
                        repository.addNote(
                            book = book,
                            paragraphIndex = quotedParagraphIndex,
                            sentence = quotedParagraph,
                            translationText = answer,
                            noteText = content,
                            noteType = ReaderNoteType.CHAT,
                        )
                    }
                    Unit
                }
            }
            sendingChatBookId = null
            answerResult.onSuccess {
                refreshAll()
                requestChatSuggestions(book)
            }.onFailure { error ->
                refreshAll()
                showMessage(error.message ?: "对话失败")
            }
        }
    }

    EngReadTheme(readerTheme = settings.theme) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val current = screen) {
                AppScreen.Shelf -> ShelfScreen(
                    books = books,
                    bookChats = bookChats,
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        HomeBottomBar(
                            current = AppScreen.Shelf,
                            onSelect = { screen = it },
                        )
                    },
                    onImport = {
                        importLauncher.launch(
                            arrayOf(
                                "text/plain",
                                "application/epub+zip",
                                "application/vnd.amazon.ebook",
                                "application/x-mobipocket-ebook",
                                "application/x-mobi8-ebook",
                                "application/octet-stream",
                                "*/*",
                            ),
                        )
                    },
                    onOpenBook = { openReader(it, null) },
                    onDeleteBook = { book ->
                        scope.launch {
                            withContext(Dispatchers.IO) { repository.deleteBook(book.id) }
                            refreshAll()
                            showMessage("已删除《${book.title}》")
                        }
                    },
                )

                is AppScreen.Reader -> {
                    val book = books.firstOrNull { it.id == current.bookId }
                    ReaderScreen(
                        book = book,
                        settings = settings,
                        modifier = Modifier.fillMaxSize(),
                        onBack = {
                            refreshAll()
                            screen = AppScreen.Shelf
                        },
                        onOpenNotes = {
                            refreshAll()
                            screen = AppScreen.Notes
                        },
                        onSettingsChange = { next ->
                            settings = next
                            scope.launch(Dispatchers.IO) { repository.saveSettings(next) }
                        },
                        onProgress = { paragraphIndex ->
                            scope.launch(Dispatchers.IO) {
                                repository.updateProgress(current.bookId, paragraphIndex)
                            }
                        },
                        onAddNote = { paragraphIndex, sentence, translationText, noteText ->
                            book?.let { activeBook ->
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        repository.addNote(
                                            activeBook,
                                            paragraphIndex,
                                            sentence,
                                            translationText,
                                            noteText,
                                        )
                                    }
                                    refreshAll()
                                    showMessage("已加入笔记本")
                                }
                            }
                        },
                        onChatSelection = { paragraphIndex, paragraph, question ->
                            book?.let { activeBook ->
                                preferredChatBookId = activeBook.id
                                screen = AppScreen.Chat
                                sendBookChatMessage(
                                    book = activeBook,
                                    text = question,
                                    quotedParagraph = paragraph,
                                    quotedParagraphIndex = paragraphIndex,
                                )
                            }
                        },
                        onAddLookupHistory = { paragraphIndex, type, sourceText, resultText, phonetic ->
                            book?.let { activeBook ->
                                scope.launch(Dispatchers.IO) {
                                    repository.addLookupHistory(
                                        activeBook,
                                        paragraphIndex,
                                        type,
                                        sourceText,
                                        resultText,
                                        phonetic,
                                    )
                                    lookupHistory = repository.getLookupHistory()
                                }
                            }
                        },
                    )
                }

                AppScreen.Notes -> NotesScreen(
                    notes = notes,
                    lookupHistory = lookupHistory,
                    noteFont = settings.noteFont,
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        HomeBottomBar(
                            current = AppScreen.Notes,
                            onSelect = { screen = it },
                        )
                    },
                    onExport = {
                        exportLauncher.launch("engread-notes.md")
                    },
                    onUpdateNote = { note, text ->
                        scope.launch {
                            withContext(Dispatchers.IO) { repository.updateNote(note.id, text) }
                            refreshAll()
                            showMessage("笔记已更新")
                        }
                    },
                    onDeleteNote = { note ->
                        scope.launch {
                            withContext(Dispatchers.IO) { repository.deleteNote(note.id) }
                            refreshAll()
                            showUndoMessage("笔记已删除") {
                                repository.restoreNote(note)
                            }
                        }
                    },
                    onDeleteSelectedItems = { selectedNotes, selectedEntries ->
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                selectedNotes.forEach { repository.deleteNote(it.id) }
                                selectedEntries.forEach { repository.deleteLookupHistory(it.id) }
                            }
                            refreshAll()
                            showUndoMessage("已删除 ${selectedNotes.size + selectedEntries.size} 条记录") {
                                selectedNotes.forEach { repository.restoreNote(it) }
                                selectedEntries.forEach { repository.restoreLookupHistory(it) }
                            }
                        }
                    },
                    onDeleteLookupHistory = { selectedEntries ->
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                selectedEntries.forEach { repository.deleteLookupHistory(it.id) }
                            }
                            refreshAll()
                            showUndoMessage("已删除 ${selectedEntries.size} 条查词记录") {
                                selectedEntries.forEach { repository.restoreLookupHistory(it) }
                            }
                        }
                    },
                    onClearHistory = {
                        scope.launch {
                            val removedEntries = lookupHistory
                            withContext(Dispatchers.IO) { repository.clearLookupHistory() }
                            refreshAll()
                            showUndoMessage("查词历史已清空") {
                                removedEntries.forEach { repository.restoreLookupHistory(it) }
                            }
                        }
                    },
                    onOpenSource = { bookId, paragraphIndex ->
                        books.firstOrNull { it.id == bookId }?.let { book ->
                            openReader(book, paragraphIndex)
                        } ?: showMessage("找不到原书")
                    },
                )

                AppScreen.Chat -> ChatScreen(
                    books = books,
                    bookChats = bookChats,
                    sendingBookId = sendingChatBookId,
                    suggestingBookId = suggestingChatBookId,
                    focusBookId = preferredChatBookId,
                    suggestionsByBook = chatSuggestionsByBook,
                    settings = settings,
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        HomeBottomBar(
                            current = AppScreen.Chat,
                            onSelect = { screen = it },
                        )
                    },
                    onSendMessage = { book, text -> sendBookChatMessage(book, text) },
                    onRefreshSuggestions = { book -> requestChatSuggestions(book) },
                    onOpenReader = { book, paragraphIndex -> openReader(book, paragraphIndex) },
                    onAddLookupHistory = { activeBook, paragraphIndex, type, sourceText, resultText, phonetic ->
                        scope.launch(Dispatchers.IO) {
                            repository.addLookupHistory(
                                activeBook,
                                paragraphIndex,
                                type,
                                sourceText,
                                resultText,
                                phonetic,
                            )
                            lookupHistory = repository.getLookupHistory()
                        }
                    },
                )

                AppScreen.Settings -> SettingsScreen(
                    settings = settings,
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        HomeBottomBar(
                            current = AppScreen.Settings,
                            onSelect = { screen = it },
                        )
                    },
                    onSettingsChange = { next ->
                        settings = next
                        scope.launch(Dispatchers.IO) { repository.saveSettings(next) }
                    },
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
            )
            UndoNoticeHost(
                notices = undoNotices,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
                onDismiss = { dismissUndoNotice(it) },
                onAction = { notice ->
                    dismissUndoNotice(notice.id)
                    scope.launch {
                        withContext(Dispatchers.IO) { notice.onAction() }
                        refreshAll()
                        showMessage("已恢复")
                    }
                },
            )
        }
    }
}

@Composable
private fun UndoNoticeHost(
    notices: List<UndoNotice>,
    modifier: Modifier = Modifier,
    onDismiss: (String) -> Unit,
    onAction: (UndoNotice) -> Unit,
) {
    if (notices.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        notices.forEach { notice ->
            LaunchedEffect(notice.id) {
                delay(6_000)
                onDismiss(notice.id)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = notice.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onAction(notice) }) {
                        Text(
                            text = notice.actionLabel,
                            color = MaterialTheme.colorScheme.inversePrimary,
                        )
                    }
                    IconButton(
                        onClick = { onDismiss(notice.id) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeBottomBar(
    current: AppScreen,
    onSelect: (AppScreen) -> Unit,
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = Color.Transparent,
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = current == AppScreen.Shelf,
            onClick = { onSelect(AppScreen.Shelf) },
            icon = { Icon(Icons.Filled.AutoStories, contentDescription = null) },
            label = { Text("书架") },
            colors = itemColors,
        )
        NavigationBarItem(
            selected = current == AppScreen.Notes,
            onClick = { onSelect(AppScreen.Notes) },
            icon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
            label = { Text("笔记") },
            colors = itemColors,
        )
        NavigationBarItem(
            selected = current == AppScreen.Chat,
            onClick = { onSelect(AppScreen.Chat) },
            icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
            label = { Text("对话") },
            colors = itemColors,
        )
        NavigationBarItem(
            selected = current == AppScreen.Settings,
            onClick = { onSelect(AppScreen.Settings) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("设置") },
            colors = itemColors,
        )
    }
}

private fun List<WordEntry>.replaceAtOrAppend(index: Int, entry: WordEntry): List<WordEntry> =
    if (index in indices) {
        mapIndexed { itemIndex, item -> if (itemIndex == index) entry else item }
    } else {
        this + entry
    }

private fun WordEntry.toLookupHistoryText(): String =
    buildString {
        append(meaning)
        if (root.isNotBlank()) append("\n词根：").append(root)
        if (cognates.isNotEmpty()) append("\n同源词：").append(cognates.joinToString("；"))
        if (synonyms.isNotEmpty()) append("\n近义词：").append(synonyms.joinToString("；"))
    }

private fun WordEntry.usIpa(): String =
    usPhonetic.ifBlank { phonetic }.ifBlank { "未知" }

private fun WordEntry.ukIpa(): String =
    ukPhonetic.ifBlank { phonetic }.ifBlank { "未知" }

private fun WordEntry.historyPhoneticText(): String =
    "美 ${usIpa()} · 英 ${ukIpa()}"

private fun WordEntry.needsLlmWordDetails(): Boolean =
    root.isBlank() || cognates.isEmpty() || synonyms.isEmpty() || usPhonetic.isBlank() || ukPhonetic.isBlank()

private fun WordEntry.mergeLlmWordDetails(enriched: WordEntry): WordEntry =
    copy(
        word = enriched.word.ifBlank { word },
        phonetic = phonetic.takeUnless { it.isBlank() || it == "未知" } ?: enriched.phonetic,
        meaning = meaning.takeIf { it.isNotBlank() && it != "暂无释义" } ?: enriched.meaning,
        root = root.ifBlank { enriched.root },
        cognates = cognates.ifEmpty { enriched.cognates },
        synonyms = synonyms.ifEmpty { enriched.synonyms },
        usPhonetic = usPhonetic.ifBlank { enriched.usPhonetic },
        ukPhonetic = ukPhonetic.ifBlank { enriched.ukPhonetic },
        detailsLoading = false,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfScreen(
    books: List<Book>,
    bookChats: List<BookChat>,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    onImport: () -> Unit,
    onOpenBook: (Book) -> Unit,
    onDeleteBook: (Book) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<Book?>(null) }
    var pendingDeleteSecondConfirm by remember { mutableStateOf<Book?>(null) }
    fun hasChat(book: Book): Boolean =
        bookChats.any { it.bookId == book.id && (it.summary.isNotBlank() || it.messages.isNotEmpty()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "EngRead",
                        fontWeight = FontWeight.Black,
                    )
                },
                actions = {
                    IconButton(onClick = onImport) {
                        Icon(Icons.Filled.Add, contentDescription = "导入书籍")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        if (books.isEmpty()) {
            EmptyShelf(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                onImport = onImport,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(books, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        onClick = { onOpenBook(book) },
                        onDelete = { pendingDelete = book },
                    )
                }
            }
        }
    }

    pendingDelete?.let { book ->
        val bookHasChat = hasChat(book)
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text(if (bookHasChat) "这本书有对话记录" else "删除书籍？") },
            text = {
                Text(
                    if (bookHasChat) {
                        "《${book.title}》已有对话内容。删除会同时移除书籍、笔记、查词和对话记录，需要再次确认。"
                    } else {
                        "《${book.title}》和它的笔记会从本机移除。"
                    },
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDelete = null
                        if (bookHasChat) {
                            pendingDeleteSecondConfirm = book
                        } else {
                            onDeleteBook(book)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(if (bookHasChat) "继续" else "删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("取消")
                }
            },
        )
    }

    pendingDeleteSecondConfirm?.let { book ->
        AlertDialog(
            onDismissRequest = { pendingDeleteSecondConfirm = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("确认删除？") },
            text = { Text("将永久删除《${book.title}》及其对话记录。") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeleteSecondConfirm = null
                        onDeleteBook(book)
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("确认删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteSecondConfirm = null }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun EmptyShelf(
    modifier: Modifier,
    onImport: () -> Unit,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.AutoStories,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp),
                )
            }
            Text(
                text = "书架还是空的",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "导入一本英文书，开始边读边积累词汇和笔记。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ElevatedButton(
                onClick = onImport,
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("选择 TXT / EPUB / MOBI / AZW3")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(
    books: List<Book>,
    bookChats: List<BookChat>,
    sendingBookId: String?,
    suggestingBookId: String?,
    focusBookId: String,
    suggestionsByBook: Map<String, List<String>>,
    settings: ReaderSettings,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    onSendMessage: (Book, String) -> Unit,
    onRefreshSuggestions: (Book) -> Unit,
    onOpenReader: (Book, Int?) -> Unit,
    onAddLookupHistory: (Book, Int, LookupHistoryType, String, String, String) -> Unit,
) {
    var selectedBookId by rememberSaveable { mutableStateOf("") }
    val preferredBookId = remember(books, bookChats, focusBookId) {
        focusBookId.takeIf { id -> books.any { it.id == id } }
            ?: bookChats.firstOrNull { chat -> books.any { it.id == chat.bookId } }?.bookId
            ?: books.firstOrNull()?.id
            ?: ""
    }
    LaunchedEffect(preferredBookId, books.map { it.id }, focusBookId) {
        if (selectedBookId.isBlank() || books.none { it.id == selectedBookId }) {
            selectedBookId = preferredBookId
        }
    }
    val selectedBook = books.firstOrNull { it.id == selectedBookId }
    val selectedChat = selectedBook?.let { book ->
        bookChats.firstOrNull { it.bookId == book.id }
    }
    val messages = selectedChat?.messages.orEmpty()
    val listState = rememberLazyListState()
    val isSending = selectedBook != null && sendingBookId == selectedBook.id
    val suggestionsLoading = selectedBook != null && suggestingBookId == selectedBook.id
    val remoteSuggestions = selectedBook?.let { suggestionsByBook[it.id] }.orEmpty()
    var localSuggestions by rememberSaveable(selectedBookId) {
        mutableStateOf(defaultReadingQuestions.randomThree())
    }
    var localSuggestionsLoading by rememberSaveable(selectedBookId) { mutableStateOf(false) }
    var initialSuggestionsRequested by rememberSaveable(selectedBookId) { mutableStateOf(false) }
    val visibleSuggestions = remoteSuggestions.ifEmpty { localSuggestions }
    val suggestionsAreLoading = suggestionsLoading || localSuggestionsLoading
    var input by rememberSaveable(selectedBookId) { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dictionary = remember(context) { EcdictDictionary(context.applicationContext) }
    var wordStack by remember(selectedBookId) { mutableStateOf<List<WordEntry>>(emptyList()) }
    var wordLookupSerial by remember(selectedBookId) { mutableStateOf(0) }
    var ttsAccent by rememberSaveable { mutableStateOf(TtsAccent.US) }
    val showLatestButton = selectedBook != null && messages.isNotEmpty() && listState.canScrollForward

    fun refreshLocalSuggestions() {
        if (localSuggestionsLoading) return
        scope.launch {
            localSuggestionsLoading = true
            delay(360)
            localSuggestions = defaultReadingQuestions.randomThree()
            localSuggestionsLoading = false
        }
    }

    LaunchedEffect(selectedBookId, selectedBook?.id, settings.translation.isConfigured, remoteSuggestions.isEmpty()) {
        val book = selectedBook ?: return@LaunchedEffect
        if (settings.translation.isConfigured && remoteSuggestions.isEmpty() && !initialSuggestionsRequested) {
            initialSuggestionsRequested = true
            onRefreshSuggestions(book)
        }
    }

    fun closeTopWordCard() {
        val nextStack = wordStack.dropLast(1)
        wordStack = nextStack
        if (nextStack.isEmpty()) {
            wordLookupSerial += 1
        }
    }

    fun lookupChatWord(word: String) {
        val book = selectedBook ?: return
        val normalizedWord = word.trim()
        if (normalizedWord.isBlank()) return
        val paragraphIndex = book.lastReadParagraph
        val contextText = book.paragraphs.getOrNull(paragraphIndex)
            .orEmpty()
            .ifBlank { messages.takeLast(6).joinToString("\n") { it.content } }
        val requestSerial = wordLookupSerial
        val dictionaryEntry = runCatching { dictionary.lookup(normalizedWord) }.getOrNull()
        if (dictionaryEntry != null) {
            val shouldEnrichDetails = dictionaryEntry.needsLlmWordDetails() && settings.translation.isConfigured
            val targetIndex = wordStack.size
            val immediateEntry = dictionaryEntry.copy(detailsLoading = shouldEnrichDetails)
            wordStack = wordStack + immediateEntry
            onAddLookupHistory(
                book,
                paragraphIndex,
                LookupHistoryType.WORD,
                immediateEntry.word,
                immediateEntry.toLookupHistoryText(),
                immediateEntry.historyPhoneticText(),
            )
            if (shouldEnrichDetails) {
                scope.launch {
                    val result = runCatching {
                        OpenAiWordLookup.lookup(normalizedWord, contextText, settings.translation)
                    }
                    result.onSuccess { enriched ->
                        if (requestSerial != wordLookupSerial) return@onSuccess
                        val merged = immediateEntry.mergeLlmWordDetails(enriched)
                        wordStack = wordStack.replaceAtOrAppend(targetIndex, merged)
                        onAddLookupHistory(
                            book,
                            paragraphIndex,
                            LookupHistoryType.WORD,
                            merged.word,
                            merged.toLookupHistoryText(),
                            merged.historyPhoneticText(),
                        )
                    }.onFailure {
                        if (requestSerial != wordLookupSerial) return@onFailure
                        wordStack = wordStack.replaceAtOrAppend(
                            targetIndex,
                            immediateEntry.copy(detailsLoading = false),
                        )
                    }
                }
            }
            return
        }
        if (!settings.translation.isConfigured) {
            wordStack = wordStack + WordEntry(
                word = normalizedWord,
                phonetic = "未知",
                meaning = "本地词典未收录。请在设置中配置 API 后查阅。",
            )
            return
        }
        val targetIndex = wordStack.size
        wordStack = wordStack + WordEntry(
            word = normalizedWord,
            phonetic = "查询中...",
            meaning = "本地词典未收录，查阅中...",
        )
        scope.launch {
            val result = runCatching {
                OpenAiWordLookup.lookup(normalizedWord, contextText, settings.translation)
            }
            result.onSuccess { entry ->
                if (requestSerial != wordLookupSerial) return@onSuccess
                wordStack = wordStack.replaceAtOrAppend(targetIndex, entry)
                onAddLookupHistory(
                    book,
                    paragraphIndex,
                    LookupHistoryType.WORD,
                    entry.word,
                    entry.toLookupHistoryText(),
                    entry.historyPhoneticText(),
                )
            }.onFailure { error ->
                if (requestSerial != wordLookupSerial) return@onFailure
                wordStack = wordStack.replaceAtOrAppend(
                    targetIndex,
                    WordEntry(
                        word = normalizedWord,
                        phonetic = "查询失败",
                        meaning = error.message ?: "查词失败",
                    ),
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = selectedBook?.title ?: "对话",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Black,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            Column {
                if (showLatestButton && wordStack.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 1.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
                            modifier = Modifier
                                .heightIn(min = 24.dp)
                                .clickable {
                                    scope.launch {
                                        val target = listState.layoutInfo.totalItemsCount - 1
                                        if (target >= 0) listState.scrollToItem(target)
                                    }
                                },
                        ) {
                            Text(
                                text = "看最新",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
                wordStack.lastOrNull()?.let { entry ->
                    WordLookupPanel(
                        entry = entry,
                        stackDepth = wordStack.size,
                        ttsAccent = ttsAccent,
                        onClose = { closeTopWordCard() },
                        onLookupWord = { lookupChatWord(it) },
                        onTtsAccentChange = { ttsAccent = it },
                        onSpeak = {
                            Toast.makeText(context, "可在阅读页使用系统 TTS 播放读音", Toast.LENGTH_SHORT).show()
                        },
                    )
                } ?: ChatInputBar(
                    value = input,
                    enabled = selectedBook != null && !isSending,
                    sending = isSending,
                    onValueChange = { input = it },
                    onSend = {
                        val book = selectedBook ?: return@ChatInputBar
                        val text = input
                        input = ""
                        onSendMessage(book, text)
                    },
                )
                bottomBar()
            }
        },
    ) { padding ->
        if (books.isEmpty()) {
            EmptyChat(modifier = Modifier.padding(padding).fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                BookChatSelector(
                    books = books,
                    selectedBook = selectedBook,
                    bookChats = bookChats,
                    onSelect = { selectedBookId = it.id },
                    onContinueRead = {
                        selectedBook?.let { book -> onOpenReader(book, null) }
                    },
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (messages.isEmpty()) {
                        item {
                            EmptyChatPrompt()
                        }
                    }
                    itemsIndexed(messages, key = { _, message -> message.id }) { _, message ->
                        ChatMessageBubble(
                            message = message,
                            onLookupWord = { lookupChatWord(it) },
                            onOpenAnchor = { paragraphIndex ->
                                selectedBook?.let { onOpenReader(it, paragraphIndex) }
                            },
                        )
                    }
                    if (isSending) {
                        item {
                            ChatThinkingBubble()
                        }
                    }
                    if (!isSending) {
                        item {
                            ChatSuggestionsPanel(
                                questions = visibleSuggestions,
                                loading = suggestionsAreLoading,
                                onQuestionClick = { question ->
                                    val book = selectedBook ?: return@ChatSuggestionsPanel
                                    onSendMessage(book, question)
                                },
                                onRefresh = {
                                    val book = selectedBook ?: return@ChatSuggestionsPanel
                                    if (!settings.translation.isConfigured) {
                                        refreshLocalSuggestions()
                                    } else {
                                        onRefreshSuggestions(book)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun BookChatSelector(
    books: List<Book>,
    selectedBook: Book?,
    bookChats: List<BookChat>,
    onSelect: (Book) -> Unit,
    onContinueRead: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AssistChip(
                onClick = { menuOpen = true },
                label = {
                    Text(
                        text = selectedBook?.title ?: "选择书籍",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                leadingIcon = { Icon(Icons.Filled.AutoStories, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
            )
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                books.forEach { book ->
                    val hasChat = bookChats.any { it.bookId == book.id && it.messages.isNotEmpty() }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (hasChat) "${book.title} · 有对话" else book.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onSelect(book)
                        },
                    )
                }
            }
        }
        IconButton(
            onClick = onContinueRead,
            enabled = selectedBook != null,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "继续读",
                tint = if (selectedBook != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyChatPrompt() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "可以问这本书的人物、情节、词句和读书计划。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChatThinkingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text("思考中", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ChatSuggestionsPanel(
    questions: List<String>,
    loading: Boolean,
    onQuestionClick: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "猜你想问",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            IconButton(onClick = onRefresh, enabled = !loading) {
                Icon(Icons.Filled.Refresh, contentDescription = "换一批")
            }
        }
        if (loading) {
            val brush = ShimmerBrush()
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (index == 2) 0.82f else 1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush),
                )
            }
        } else {
            questions.take(3).forEach { question ->
                Surface(
                    onClick = { onQuestionClick(question) },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = question,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onLookupWord: (String) -> Unit,
    onOpenAnchor: (Int) -> Unit,
) {
    val fromUser = message.role == ChatRole.USER
    var detailsVisible by remember(message.id) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.86f),
            horizontalAlignment = if (fromUser) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (fromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                modifier = Modifier.pointerInput(message.id) {
                    detectTapGestures(onTap = { detailsVisible = !detailsVisible })
                },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (!fromUser) {
                        Text(
                            text = "EngRead",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    MarkdownText(
                        markdown = message.content,
                        color = MaterialTheme.colorScheme.onSurface,
                        onLookupWord = onLookupWord,
                        onOpenAnchor = onOpenAnchor,
                    )
                }
            }
            if (detailsVisible) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatChatMessageTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    )
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(message.content))
                            Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "复制",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun formatChatMessageTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = timestamp }
    val sameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    val pattern = if (sameDay) "HH:mm" else "yyyy年MM月dd日 HH:mm"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
}

private val chatMarkdownParser: Parser by lazy {
    Parser.builder()
        .extensions(listOf(TablesExtension.create()))
        .build()
}

private const val ChatAnchorAnnotation = "engread-paragraph-anchor"

@Composable
private fun MarkdownText(
    markdown: String,
    color: Color,
    onLookupWord: (String) -> Unit = {},
    onOpenAnchor: (Int) -> Unit = {},
) {
    val document = remember(markdown) { chatMarkdownParser.parse(markdown.trim()) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        MarkdownNodeChildren(
            parent = document,
            color = color,
            onLookupWord = onLookupWord,
            onOpenAnchor = onOpenAnchor,
        )
    }
}

@Composable
private fun MarkdownInlineText(
    annotated: AnnotatedString,
    style: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    onLookupWord: (String) -> Unit,
    onOpenAnchor: (Int) -> Unit = {},
) {
    var layoutResult by remember(annotated.text) { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = annotated,
        style = style,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        modifier = modifier.pointerInput(annotated.text) {
            detectTapGestures(
                onTap = { position ->
                    val offset = layoutResult?.getOffsetForPosition(position) ?: return@detectTapGestures
                    val paragraphIndex = annotated
                        .getStringAnnotations(ChatAnchorAnnotation, offset, offset)
                        .firstOrNull()
                        ?.item
                        ?.toIntOrNull()
                        ?: return@detectTapGestures
                    onOpenAnchor(paragraphIndex)
                },
                onLongPress = { position ->
                    val offset = layoutResult?.getOffsetForPosition(position) ?: return@detectTapGestures
                    val word = extractWordAt(annotated.text, offset) ?: return@detectTapGestures
                    onLookupWord(word)
                },
            )
        },
        onTextLayout = { layoutResult = it },
    )
}

@Composable
private fun MarkdownNodeChildren(
    parent: Node,
    color: Color,
    onLookupWord: (String) -> Unit,
    onOpenAnchor: (Int) -> Unit,
) {
    parent.childNodes().forEach { child ->
        MarkdownBlockNode(
            node = child,
            color = color,
            onLookupWord = onLookupWord,
            onOpenAnchor = onOpenAnchor,
        )
    }
}

@Composable
private fun MarkdownBlockNode(
    node: Node,
    color: Color,
    onLookupWord: (String) -> Unit,
    onOpenAnchor: (Int) -> Unit,
) {
    when (node) {
        is Paragraph -> {
            MarkdownInlineText(
                annotated = remember(node) { markdownInlineFromChildren(node) },
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                onLookupWord = onLookupWord,
                onOpenAnchor = onOpenAnchor,
            )
        }

        is Heading -> {
            MarkdownInlineText(
                annotated = remember(node) { markdownInlineFromChildren(node) },
                style = when (node.level.coerceIn(1, 3)) {
                    1 -> MaterialTheme.typography.titleMedium
                    2 -> MaterialTheme.typography.titleSmall
                    else -> MaterialTheme.typography.bodyLarge
                },
                color = color,
                fontWeight = FontWeight.Bold,
                onLookupWord = onLookupWord,
                onOpenAnchor = onOpenAnchor,
            )
        }

        is BlockQuote -> {
            Column(
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                node.childNodes().forEach { child ->
                    MarkdownBlockNode(
                        node = child,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        onLookupWord = onLookupWord,
                        onOpenAnchor = onOpenAnchor,
                    )
                }
            }
        }

        is BulletList -> {
            MarkdownListBlock(
                list = node,
                color = color,
                onLookupWord = onLookupWord,
                onOpenAnchor = onOpenAnchor,
            )
        }

        is OrderedList -> {
            MarkdownListBlock(
                list = node,
                color = color,
                onLookupWord = onLookupWord,
                onOpenAnchor = onOpenAnchor,
            )
        }

        is FencedCodeBlock -> {
            if (node.info.trim().lowercase(Locale.US).startsWith("mermaid")) {
                MermaidBlock(node.literal)
            } else {
                MarkdownCodeBlock(node.literal)
            }
        }

        is IndentedCodeBlock -> {
            MarkdownCodeBlock(node.literal)
        }

        is TableBlock -> {
            MarkdownTableBlock(
                table = node,
                color = color,
                onLookupWord = onLookupWord,
                onOpenAnchor = onOpenAnchor,
            )
        }

        is ThematicBreak -> HorizontalDivider()

        else -> {
            if (node.firstChild != null) {
                MarkdownNodeChildren(
                    parent = node,
                    color = color,
                    onLookupWord = onLookupWord,
                    onOpenAnchor = onOpenAnchor,
                )
            }
        }
    }
}

@Composable
private fun MarkdownListBlock(
    list: Node,
    color: Color,
    onLookupWord: (String) -> Unit,
    onOpenAnchor: (Int) -> Unit,
) {
    val startNumber = (list as? OrderedList)?.markerStartNumber ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        list.childNodes().filterIsInstance<ListItem>().forEachIndexed { index, item ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (list is OrderedList) "${startNumber + index}." else "•",
                    color = color,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    item.childNodes().forEach { child ->
                        MarkdownBlockNode(
                            node = child,
                            color = color,
                            onLookupWord = onLookupWord,
                            onOpenAnchor = onOpenAnchor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownTableBlock(
    table: TableBlock,
    color: Color,
    onLookupWord: (String) -> Unit,
    onOpenAnchor: (Int) -> Unit,
) {
    val rows = remember(table) { table.toMarkdownRows() }
    val columnCount = (rows.maxOfOrNull { it.size } ?: 0).coerceAtLeast(1)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.horizontalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier.padding(1.dp),
        ) {
            rows.forEach { row ->
                MarkdownTableRow(
                    cells = row,
                    columnCount = columnCount,
                    color = color,
                    onLookupWord = onLookupWord,
                    onOpenAnchor = onOpenAnchor,
                )
            }
        }
    }
}

@Composable
private fun MarkdownTableRow(
    cells: List<MarkdownTableCell>,
    columnCount: Int,
    color: Color,
    onLookupWord: (String) -> Unit,
    onOpenAnchor: (Int) -> Unit,
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        repeat(columnCount) { index ->
            val cell = cells.getOrNull(index)
            val header = cell?.header == true
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    .background(if (header) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 9.dp, vertical = 8.dp),
            ) {
                MarkdownInlineText(
                    annotated = cell?.text ?: AnnotatedString(""),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (header) MaterialTheme.colorScheme.onSurfaceVariant else color,
                    fontWeight = if (header) FontWeight.Bold else null,
                    textAlign = cell?.alignment.toTextAlign(),
                    modifier = Modifier.fillMaxWidth(),
                    onLookupWord = onLookupWord,
                    onOpenAnchor = onOpenAnchor,
                )
            }
        }
    }
}

private data class MarkdownTableCell(
    val text: AnnotatedString,
    val header: Boolean,
    val alignment: TableCell.Alignment?,
)

private fun TableBlock.toMarkdownRows(): List<List<MarkdownTableCell>> {
    val rows = mutableListOf<List<MarkdownTableCell>>()
    childNodes().forEach { section ->
        val header = section is TableHead
        if (section !is TableHead && section !is TableBody) return@forEach
        section.childNodes().filterIsInstance<TableRow>().forEach { row ->
            rows += row.childNodes().filterIsInstance<TableCell>().map { cell ->
                MarkdownTableCell(
                    text = markdownInlineFromChildren(cell),
                    header = header,
                    alignment = cell.alignment,
                )
            }
        }
    }
    return rows
}

private fun TableCell.Alignment?.toTextAlign(): TextAlign =
    when (this) {
        TableCell.Alignment.LEFT -> TextAlign.Start
        TableCell.Alignment.CENTER -> TextAlign.Center
        TableCell.Alignment.RIGHT -> TextAlign.End
        null -> TextAlign.Start
    }

private fun markdownInlineFromChildren(parent: Node): AnnotatedString =
    buildAnnotatedString {
        parent.childNodes().forEach { appendMarkdownInline(it) }
    }

private fun AnnotatedString.Builder.appendMarkdownInline(node: Node) {
    when (node) {
        is MarkdownTextNode -> append(node.literal)
        is Code -> {
            pushStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color.Black.copy(alpha = 0.08f),
                ),
            )
            append(node.literal)
            pop()
        }
        is Emphasis -> {
            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            node.childNodes().forEach { appendMarkdownInline(it) }
            pop()
        }
        is StrongEmphasis -> {
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            node.childNodes().forEach { appendMarkdownInline(it) }
            pop()
        }
        is Link -> {
            val paragraphIndex = node.destination.toEngReadParagraphIndex()
            if (paragraphIndex != null) {
                pushStringAnnotation(ChatAnchorAnnotation, paragraphIndex.toString())
            }
            pushStyle(
                SpanStyle(
                    color = Color(0xFF58CC02),
                    textDecoration = TextDecoration.Underline,
                ),
            )
            node.childNodes().forEach { appendMarkdownInline(it) }
            pop()
            if (paragraphIndex != null) pop()
        }
        is SoftLineBreak -> append(" ")
        is HardLineBreak -> append("\n")
        is HtmlInline -> append(node.literal)
        else -> node.childNodes().forEach { appendMarkdownInline(it) }
    }
}

private fun String.toEngReadParagraphIndex(): Int? {
    val trimmed = trim()
    val prefix = "engread://paragraph/"
    if (!trimmed.startsWith(prefix)) return null
    return trimmed.removePrefix(prefix).takeWhile { it.isDigit() }.toIntOrNull()
}

private fun Node.childNodes(): List<Node> =
    buildList {
        var child = firstChild
        while (child != null) {
            add(child)
            child = child.next
        }
    }

@Composable
private fun MermaidBlock(code: String) {
    val html = remember(code) { mermaidHtml(code) }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp, max = 420.dp),
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 420.dp),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL("https://engread.local/", html, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://engread.local/", html, "text/html", "UTF-8", null)
            },
        )
    }
}

private fun mermaidHtml(code: String): String {
    val escapedCode = code.escapeHtml()
    return """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            html, body {
              margin: 0;
              padding: 8px;
              background: transparent;
              color: #1f2933;
              font-family: sans-serif;
            }
            .mermaid {
              width: max-content;
              min-width: 100%;
            }
            svg {
              max-width: none;
            }
          </style>
          <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
        </head>
        <body>
          <pre class="mermaid">$escapedCode</pre>
          <script>
            mermaid.initialize({ startOnLoad: true, theme: 'neutral', securityLevel: 'loose' });
          </script>
        </body>
        </html>
    """.trimIndent()
}

private fun String.escapeHtml(): String =
    buildString {
        this@escapeHtml.forEach { char ->
            when (char) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                else -> append(char)
            }
        }
    }

@Composable
private fun MarkdownCodeBlock(code: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(10.dp),
        )
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    enabled: Boolean,
    sending: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                placeholder = { Text("和这本书聊聊...") },
                minLines = 1,
                maxLines = 5,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 54.dp, max = 144.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
            IconButton(
                onClick = onSend,
                enabled = enabled && value.trim().isNotBlank() && !sending,
            ) {
                if (sending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
                }
            }
        }
    }
}

@Composable
private fun EmptyChat(modifier: Modifier) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Chat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(42.dp),
            )
            Text("先导入一本书，再开始对话", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookCard(
    book: Book,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val progressPercent = (book.progress * 100).roundToInt().coerceIn(0, 100)
    val paragraphCount = book.paragraphs.size
    val currentParagraph = if (paragraphCount == 0) {
        0
    } else {
        (book.lastReadParagraph + 1).coerceIn(1, paragraphCount)
    }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.AutoStories,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = book.fileName.ifBlank { book.sourceType.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("删除") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            },
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = book.sourceType.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    )
                }
                Text(
                    text = "$progressPercent%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (paragraphCount > 0) {
                        "第 $currentParagraph / $paragraphCount 段"
                    } else {
                        "暂无段落"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            LinearProgressIndicator(
                progress = { book.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Text(
                text = formatTimestamp(book.updatedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class ReaderPageAnchor(
    val paragraphIndex: Int,
    val paragraphOffset: Int,
)

private data class ChapterTocItem(
    val index: Int,
    val chapter: BookChapter,
    val startPageIndex: Int,
    val endPageIndex: Int,
    val progressPageIndex: Int?,
    val progressAnchor: ReaderPageAnchor?,
)

private enum class TtsAccent(
    val label: String,
    val locale: Locale,
) {
    US("美音", Locale.US),
    UK("英音", Locale.UK),
}

private fun TextToSpeech.availableLocalEnglishAccents(): Map<TtsAccent, Voice?> =
    TtsAccent.entries.associateWith { accent ->
        findLocalEnglishVoice(accent)
    }.filter { (accent, voice) ->
        voice != null || isLanguageInstalled(accent.locale)
    }

private fun TextToSpeech.findLocalEnglishVoice(accent: TtsAccent): Voice? {
    val targetCountry = accent.locale.country.uppercase(Locale.ROOT)
    return voices
        ?.asSequence()
        ?.filter { voice ->
            !voice.isNetworkConnectionRequired &&
                voice.locale.language.equals("en", ignoreCase = true) &&
                voice.locale.country.equals(targetCountry, ignoreCase = true)
        }
        ?.sortedWith(
            compareByDescending<Voice> { it.quality }
                .thenBy { it.latency }
                .thenBy { it.name },
        )
        ?.firstOrNull()
}

private fun TextToSpeech.isLanguageInstalled(locale: Locale): Boolean =
    isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE

private val KnownTtsEnginePackages = listOf(
    "com.oplus.ttsaccessibilityengine",
    "com.google.android.tts",
    "com.google.android.googlequicksearchbox",
    "com.heytap.speechassist",
)

private fun Context.defaultTtsEnginePackage(): String? =
    Settings.Secure.getString(contentResolver, "tts_default_synth")
        ?.takeUnless { it.isBlank() || it == "null" }

private fun Context.isPackageInstalled(packageName: String): Boolean =
    runCatching {
        packageManager.getPackageInfo(packageName, 0)
        true
    }.getOrDefault(false)

private fun Context.installedTtsEnginePackages(): List<String> {
    val queriedPackages = packageManager
        .queryIntentServices(Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0)
        .mapNotNull { it.serviceInfo?.packageName }
    val knownInstalledPackages = KnownTtsEnginePackages.filter { isPackageInstalled(it) }
    return (queriedPackages + knownInstalledPackages).distinct()
}

private fun ReaderPageAnchor.isAfter(other: ReaderPageAnchor): Boolean =
    paragraphIndex > other.paragraphIndex ||
        (paragraphIndex == other.paragraphIndex && paragraphOffset > other.paragraphOffset)

private fun List<ReaderPage>.findPageIndexForAnchor(anchor: ReaderPageAnchor): Int =
    indexOfFirst { page -> page.containsAnchor(anchor.paragraphIndex, anchor.paragraphOffset) }
        .takeIf { it >= 0 }
        ?: indexOfFirst { page -> anchor.paragraphIndex in page.firstParagraphIndex..page.lastParagraphIndex }
            .takeIf { it >= 0 }
        ?: 0

private fun List<BookChapter>.chapterForParagraph(paragraphIndex: Int): BookChapter? =
    lastOrNull { it.paragraphIndex <= paragraphIndex } ?: firstOrNull()

private fun Book.readerChapters(): List<BookChapter> {
    val tocChapters = toc
        .mapNotNull { entry ->
            val title = entry.title.trim()
            if (title.isBlank() || paragraphs.isEmpty()) {
                null
            } else {
                BookChapter(
                    title = title,
                    paragraphIndex = entry.paragraphIndex.coerceIn(0, paragraphs.lastIndex),
                )
            }
        }
        .distinctBy { it.paragraphIndex to it.title }
        .sortedBy { it.paragraphIndex }
    return tocChapters.ifEmpty { buildBookChapters(paragraphs) }
}

private fun buildChapterTocItems(
    chapters: List<BookChapter>,
    pages: List<ReaderPage>,
    chapterProgressAnchors: Map<Int, ReaderPageAnchor>,
): List<ChapterTocItem> =
    chapters.mapIndexed { index, chapter ->
        val nextChapterStart = chapters.getOrNull(index + 1)?.paragraphIndex ?: Int.MAX_VALUE
        val startPageIndex = pages.indexOfFirst { page ->
            chapter.paragraphIndex in page.firstParagraphIndex..page.lastParagraphIndex
        }.takeIf { it >= 0 } ?: 0
        val endPageIndex = pages.indexOfLast { page ->
            page.firstParagraphIndex < nextChapterStart && page.lastParagraphIndex >= chapter.paragraphIndex
        }.takeIf { it >= startPageIndex } ?: startPageIndex
        val savedAnchor = chapterProgressAnchors[chapter.paragraphIndex]
        val progressAnchor = when {
            savedAnchor == null -> null
            savedAnchor.paragraphIndex < chapter.paragraphIndex -> null
            savedAnchor.paragraphIndex >= nextChapterStart -> {
                val endPage = pages[endPageIndex]
                ReaderPageAnchor(endPage.firstParagraphIndex, endPage.firstParagraphOffset)
            }
            else -> savedAnchor
        }
        val progressPageIndex = progressAnchor?.let { anchor ->
            pages.findPageIndexForAnchor(anchor).coerceIn(startPageIndex, endPageIndex)
        }
        ChapterTocItem(
            index = index,
            chapter = chapter,
            startPageIndex = startPageIndex,
            endPageIndex = endPageIndex,
            progressPageIndex = progressPageIndex,
            progressAnchor = progressAnchor,
        )
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderScreen(
    book: Book?,
    settings: ReaderSettings,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenNotes: () -> Unit,
    onSettingsChange: (ReaderSettings) -> Unit,
    onProgress: (Int) -> Unit,
    onAddNote: (paragraphIndex: Int, sentence: String, translationText: String, noteText: String) -> Unit,
    onChatSelection: (paragraphIndex: Int, paragraph: String, question: String) -> Unit,
    onAddLookupHistory: (paragraphIndex: Int, LookupHistoryType, sourceText: String, resultText: String, phonetic: String) -> Unit,
) {
    if (book == null) {
        MissingBookScreen(modifier = modifier, onBack = onBack)
        return
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dictionary = remember(context) { EcdictDictionary(context.applicationContext) }
    val configuration = LocalConfiguration.current
    val pageCharBudget = remember(
        configuration.screenWidthDp,
        configuration.screenHeightDp,
        settings.fontSizeSp,
    ) {
        estimateReaderPageCharBudget(
            screenWidthDp = configuration.screenWidthDp,
            screenHeightDp = configuration.screenHeightDp,
            fontSizeSp = settings.fontSizeSp,
        )
    }
    val chapters = remember(book.id, book.paragraphs, book.toc) { book.readerChapters() }
    val chapterParagraphIndices = remember(chapters) { chapters.map { it.paragraphIndex }.toSet() }
    val pages = remember(book.id, book.paragraphs, pageCharBudget, chapterParagraphIndices) {
        buildReaderPages(
            paragraphs = book.paragraphs,
            maxChars = pageCharBudget,
            chapterParagraphIndices = chapterParagraphIndices,
        )
    }
    var savedAnchorParagraph by rememberSaveable(book.id) {
        mutableStateOf(book.lastReadParagraph)
    }
    var savedAnchorOffset by rememberSaveable(book.id) {
        mutableStateOf(0)
    }
    var pageAnchor by remember(book.id) {
        mutableStateOf(ReaderPageAnchor(savedAnchorParagraph, savedAnchorOffset))
    }
    var maxProgressAnchor by remember(book.id) {
        mutableStateOf(ReaderPageAnchor(book.lastReadParagraph, 0))
    }
    val chapterProgressAnchors = remember(book.id, chapters) {
        mutableStateMapOf<Int, ReaderPageAnchor>().also { anchors ->
            chapters.chapterForParagraph(book.lastReadParagraph)?.let { chapter ->
                anchors[chapter.paragraphIndex] = ReaderPageAnchor(book.lastReadParagraph, 0)
            }
        }
    }
    var pageIndex by remember(book.id) {
        mutableStateOf(pages.findPageIndexForAnchor(pageAnchor))
    }
    LaunchedEffect(pageCharBudget, pages.size) {
        val anchoredIndex = pages.findPageIndexForAnchor(pageAnchor)
        if (anchoredIndex != pageIndex) pageIndex = anchoredIndex
    }
    val page = pages[pageIndex.coerceIn(0, pages.lastIndex)]
    var tocOpen by remember { mutableStateOf(false) }
    var pageSettingsOpen by remember { mutableStateOf(false) }
    var noteSelectionText by remember { mutableStateOf<String?>(null) }
    var noteTranslationText by remember(book.id) { mutableStateOf("") }
    var noteTranslationLoading by remember(book.id) { mutableStateOf(false) }
    var noteTranslationIsError by remember(book.id) { mutableStateOf(false) }
    var chatSelectionText by remember(book.id) { mutableStateOf<String?>(null) }
    var chatSelectionParagraphIndex by remember(book.id) { mutableStateOf(0) }
    var wordStack by remember(book.id) { mutableStateOf<List<WordEntry>>(emptyList()) }
    var wordLookupSerial by remember(book.id) { mutableStateOf(0) }
    var translationText by remember(book.id) { mutableStateOf<String?>(null) }
    var translationSourceText by remember(book.id) { mutableStateOf("") }
    var translationLoading by remember(book.id) { mutableStateOf(false) }
    var translationIsError by remember(book.id) { mutableStateOf(false) }
    var selectionStart by remember(page.index) { mutableStateOf<Int?>(null) }
    var selectionEnd by remember(page.index) { mutableStateOf<Int?>(null) }
    var selectionTipVisible by remember(page.index) { mutableStateOf(false) }
    var wordSelectionRange by remember(page.index) { mutableStateOf<IntRange?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var ttsStatusText by remember { mutableStateOf("TTS 正在初始化") }
    var ttsAccent by rememberSaveable { mutableStateOf(TtsAccent.US) }
    var ttsVoices by remember { mutableStateOf<Map<TtsAccent, Voice?>>(emptyMap()) }
    val selectionRange = remember(selectionStart, selectionEnd) {
        val start = selectionStart
        val end = selectionEnd
        if (start == null || end == null || kotlin.math.abs(start - end) < 2) {
            null
        } else {
            minOf(start, end)..maxOf(start, end)
        }
    }
    val selectedText = remember(page.text, selectionRange) {
        selectionRange
            ?.let { range ->
                page.text.substring(range.first, (range.last + 1).coerceAtMost(page.text.length)).trim()
            }
            .orEmpty()
    }

    DisposableEffect(context) {
        val mainHandler = Handler(Looper.getMainLooper())
        ttsReady = false
        ttsStatusText = "TTS 正在初始化"
        var engine: TextToSpeech? = null
        val engineCandidates = buildList<String?> {
            add(context.defaultTtsEnginePackage())
            add(null)
            addAll(context.installedTtsEnginePackages())
        }
            .filter { it == null || it.isNotBlank() }
            .distinct()
        var engineCandidateIndex = 0

        fun attachProgressListener(activeEngine: TextToSpeech) {
            activeEngine.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit

                    override fun onDone(utteranceId: String?) = Unit

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        mainHandler.post {
                            Toast.makeText(context, "TTS 播放出错，请检查系统文字转语音设置", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        mainHandler.post {
                            Toast.makeText(context, "TTS 播放出错：$errorCode", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
            )
        }

        lateinit var startTtsEngine: (Int) -> Unit
        lateinit var finishTtsInit: (Int) -> Unit

        startTtsEngine = { index ->
            engineCandidateIndex = index
            ttsReady = false
            ttsVoices = emptyMap()
            ttsStatusText = "TTS 正在初始化"
            engine = null
            val packageName = engineCandidates.getOrNull(index)
            val nextEngine = if (packageName == null) {
                TextToSpeech(context.applicationContext) { status ->
                    mainHandler.post { finishTtsInit(status) }
                }
            } else {
                TextToSpeech(
                    context.applicationContext,
                    { status -> mainHandler.post { finishTtsInit(status) } },
                    packageName,
                )
            }
            engine = nextEngine
            attachProgressListener(nextEngine)
            tts = nextEngine
        }

        fun tryNextTtsEngineOrFail(message: String) {
            val previousEngine = engine
            engine = null
            previousEngine?.shutdown()
            val nextIndex = engineCandidateIndex + 1
            if (nextIndex < engineCandidates.size) {
                startTtsEngine(nextIndex)
            } else {
                tts = null
                ttsReady = false
                ttsVoices = emptyMap()
                ttsStatusText = message
            }
        }

        finishTtsInit = { status ->
            val activeEngine = engine
            if (activeEngine == null) {
                mainHandler.postDelayed({ finishTtsInit(status) }, 50)
            } else if (status == TextToSpeech.SUCCESS) {
                activeEngine.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                val availableVoices = activeEngine.availableLocalEnglishAccents()
                if (availableVoices.isEmpty()) {
                    mainHandler.post {
                        tts = activeEngine
                        ttsVoices = emptyMap()
                        ttsReady = true
                        ttsStatusText = "TTS 引擎已就绪，但未检测到本地英文语音，请在系统文字转语音中把语言或音色改为英文"
                    }
                } else {
                    val nextAccent = when {
                        ttsAccent in availableVoices.keys -> ttsAccent
                        TtsAccent.US in availableVoices.keys -> TtsAccent.US
                        TtsAccent.UK in availableVoices.keys -> TtsAccent.UK
                        else -> TtsAccent.US
                    }
                    availableVoices[nextAccent]?.let { activeEngine.setVoice(it) }
                        ?: activeEngine.setLanguage(nextAccent.locale)
                    mainHandler.post {
                        ttsVoices = availableVoices
                        ttsAccent = nextAccent
                        ttsReady = true
                        ttsStatusText = "TTS 已就绪：${availableVoices.keys.joinToString(" / ") { it.label }}"
                    }
                }
            } else {
                tryNextTtsEngineOrFail("手机 TTS 初始化失败，请在系统文字转语音中选择或安装语音引擎")
            }
        }

        startTtsEngine(0)
        onDispose {
            engine?.stop()
            engine?.shutdown()
            tts = null
            ttsReady = false
            ttsVoices = emptyMap()
        }
    }

    LaunchedEffect(tts) {
        delay(3_000)
        if (!ttsReady && ttsStatusText == "TTS 正在初始化") {
            ttsStatusText = "TTS 初始化超时，请检查系统文字转语音服务"
        }
    }

    LaunchedEffect(pageIndex, page.firstParagraphIndex, page.firstParagraphOffset) {
        val currentAnchor = ReaderPageAnchor(page.firstParagraphIndex, page.firstParagraphOffset)
        pageAnchor = currentAnchor
        savedAnchorParagraph = currentAnchor.paragraphIndex
        savedAnchorOffset = currentAnchor.paragraphOffset
        chapters.chapterForParagraph(currentAnchor.paragraphIndex)?.let { chapter ->
            val previousChapterAnchor = chapterProgressAnchors[chapter.paragraphIndex]
            if (previousChapterAnchor == null || currentAnchor.isAfter(previousChapterAnchor)) {
                chapterProgressAnchors[chapter.paragraphIndex] = currentAnchor
            }
        }
        if (currentAnchor.isAfter(maxProgressAnchor)) {
            maxProgressAnchor = currentAnchor
            onProgress(page.firstParagraphIndex)
        } else if (currentAnchor == maxProgressAnchor) {
            onProgress(page.firstParagraphIndex)
        }
    }

    val currentChapterTitle = remember(chapters, page.firstParagraphIndex) {
        chapters.lastOrNull { it.paragraphIndex <= page.firstParagraphIndex }?.title
            ?: chapters.firstOrNull()?.title
            ?: "开始阅读"
    }

    fun clearReaderOverlays() {
        wordLookupSerial += 1
        wordStack = emptyList()
        wordSelectionRange = null
        translationText = null
        translationLoading = false
        translationIsError = false
        selectionStart = null
        selectionEnd = null
    }

    fun goToPage(nextIndex: Int) {
        val boundedIndex = nextIndex.coerceIn(0, pages.lastIndex)
        val nextPage = pages[boundedIndex]
        pageIndex = boundedIndex
        val nextAnchor = ReaderPageAnchor(nextPage.firstParagraphIndex, nextPage.firstParagraphOffset)
        pageAnchor = nextAnchor
        savedAnchorParagraph = nextAnchor.paragraphIndex
        savedAnchorOffset = nextAnchor.paragraphOffset
        clearReaderOverlays()
    }

    fun goToAnchor(anchor: ReaderPageAnchor) {
        val nextIndex = pages.findPageIndexForAnchor(anchor)
        goToPage(nextIndex)
    }

    fun goPreviousPage() {
        if (pageIndex > 0) goToPage(pageIndex - 1)
    }

    fun goNextPage() {
        if (pageIndex < pages.lastIndex) goToPage(pageIndex + 1)
    }

    DisposableEffect(book.id, pageIndex, pages.size) {
        val previousHandler = { goPreviousPage() }
        val nextHandler = { goNextPage() }
        ReaderVolumeKeyPager.previous = previousHandler
        ReaderVolumeKeyPager.next = nextHandler
        onDispose {
            ReaderVolumeKeyPager.clear(previousHandler, nextHandler)
        }
    }

    fun jumpToParagraph(paragraphIndex: Int) {
        val nextPage = pages.indexOfFirst { paragraphIndex in it.firstParagraphIndex..it.lastParagraphIndex }
        if (nextPage >= 0) {
            goToPage(nextPage)
        }
    }

    fun openNoteDialog(sourceText: String, knownTranslation: String? = null) {
        if (sourceText.isBlank()) return
        noteSelectionText = sourceText
        if (knownTranslation != null) {
            noteTranslationText = knownTranslation
            noteTranslationLoading = false
            noteTranslationIsError = false
            return
        }
        noteTranslationText = "查阅中..."
        noteTranslationLoading = true
        noteTranslationIsError = false
        scope.launch {
            val result = runCatching {
                OpenAiChatTranslator.translate(sourceText, settings.translation)
            }
            result.onSuccess {
                noteTranslationText = it
                noteTranslationIsError = false
            }.onFailure {
                noteTranslationText = it.message ?: "翻译失败"
                noteTranslationIsError = true
            }
            noteTranslationLoading = false
        }
    }

    val tocItems = remember(chapters, pages, chapterProgressAnchors.toMap()) {
        buildChapterTocItems(
            chapters = chapters,
            pages = pages,
            chapterProgressAnchors = chapterProgressAnchors,
        )
    }

    fun closeTopWordCard() {
        val nextStack = wordStack.dropLast(1)
        wordStack = nextStack
        if (nextStack.isEmpty()) {
            wordLookupSerial += 1
            wordSelectionRange = null
        }
    }

    fun clearWordLookup() {
        wordLookupSerial += 1
        wordStack = emptyList()
        wordSelectionRange = null
    }

    fun lookupWord(
        word: String,
        paragraphIndex: Int = page.firstParagraphIndex,
        replaceStack: Boolean = false,
    ) {
        val normalizedWord = word.trim()
        if (normalizedWord.isBlank()) return
        val requestSerial = if (replaceStack) {
            wordLookupSerial += 1
            wordLookupSerial
        } else {
            wordLookupSerial
        }
        val dictionaryEntry = runCatching { dictionary.lookup(normalizedWord) }.getOrNull()
        if (dictionaryEntry != null) {
            val shouldEnrichDetails = dictionaryEntry.needsLlmWordDetails() && settings.translation.isConfigured
            val targetIndex = if (replaceStack) 0 else wordStack.size
            val immediateEntry = dictionaryEntry.copy(detailsLoading = shouldEnrichDetails)
            wordStack = if (replaceStack) listOf(immediateEntry) else wordStack + immediateEntry
            translationText = null
            onAddLookupHistory(
                paragraphIndex,
                LookupHistoryType.WORD,
                immediateEntry.word,
                immediateEntry.toLookupHistoryText(),
                immediateEntry.historyPhoneticText(),
            )
            if (shouldEnrichDetails) {
                scope.launch {
                    val result = runCatching {
                        OpenAiWordLookup.lookup(normalizedWord, page.text, settings.translation)
                    }
                    result.onSuccess { enriched ->
                        if (requestSerial != wordLookupSerial) return@onSuccess
                        val merged = immediateEntry.mergeLlmWordDetails(enriched)
                        wordStack = wordStack.replaceAtOrAppend(targetIndex, merged)
                        onAddLookupHistory(
                            paragraphIndex,
                            LookupHistoryType.WORD,
                            merged.word,
                            merged.toLookupHistoryText(),
                            merged.historyPhoneticText(),
                        )
                    }.onFailure {
                        if (requestSerial != wordLookupSerial) return@onFailure
                        wordStack = wordStack.replaceAtOrAppend(
                            targetIndex,
                            immediateEntry.copy(detailsLoading = false),
                        )
                    }
                }
            }
            return
        }
        val loadingEntry = WordEntry(
            word = normalizedWord,
            phonetic = "查询中...",
            meaning = "本地词典未收录，查阅中...",
        )
        val targetIndex = if (replaceStack) 0 else wordStack.size
        wordStack = if (replaceStack) listOf(loadingEntry) else wordStack + loadingEntry
        translationText = null
        scope.launch {
            val result = runCatching {
                OpenAiWordLookup.lookup(normalizedWord, page.text, settings.translation)
            }
            result.onSuccess { entry ->
                if (replaceStack && requestSerial != wordLookupSerial) return@onSuccess
                wordStack = wordStack.replaceAtOrAppend(targetIndex, entry)
                onAddLookupHistory(
                    paragraphIndex,
                    LookupHistoryType.WORD,
                    entry.word,
                    entry.toLookupHistoryText(),
                    entry.historyPhoneticText(),
                )
            }.onFailure { error ->
                if (replaceStack && requestSerial != wordLookupSerial) return@onFailure
                wordStack = wordStack.replaceAtOrAppend(
                    targetIndex,
                    WordEntry(
                        word = normalizedWord,
                        phonetic = "查询失败",
                        meaning = error.message ?: "查词失败",
                    ),
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            wordStack.lastOrNull()?.let { entry ->
                WordLookupPanel(
                    entry = entry,
                    stackDepth = wordStack.size,
                    ttsAccent = ttsAccent,
                    onClose = { closeTopWordCard() },
                    onLookupWord = { lookupWord(it) },
                    onTtsAccentChange = { ttsAccent = it },
                    onSpeak = {
                        val engine = tts
                        when {
                            entry.phonetic.contains("查询中") -> {
                                Toast.makeText(context, "查词完成后再播放", Toast.LENGTH_SHORT).show()
                            }

                            engine == null || !ttsReady -> {
                                Toast.makeText(context, ttsStatusText, Toast.LENGTH_SHORT).show()
                            }

                            else -> {
                                val voice = ttsVoices[ttsAccent]
                                val ready = if (voice != null) {
                                    engine.setVoice(voice) != TextToSpeech.ERROR
                                } else {
                                    engine.setLanguage(ttsAccent.locale) >= TextToSpeech.LANG_AVAILABLE
                                }
                                if (!ready) {
                                    Toast.makeText(
                                        context,
                                        "系统 TTS 引擎可用，但没有${ttsAccent.label}英文语音，请在文字转语音的语言或音色里选择英文",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    return@WordLookupPanel
                                }
                                val result = engine.speak(
                                    entry.word,
                                    TextToSpeech.QUEUE_FLUSH,
                                    Bundle().apply {
                                        putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                                    },
                                    "engread-${entry.word}-${System.currentTimeMillis()}",
                                )
                                if (result == TextToSpeech.ERROR) {
                                    Toast.makeText(context, "TTS 播放失败，请检查系统文字转语音设置", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                )
            } ?: translationText?.let { text ->
                TranslationResultPanel(
                    sourceText = translationSourceText.ifBlank { selectedText },
                    text = text,
                    loading = translationLoading,
                    isError = translationIsError,
                    onAddNote = if (translationSourceText.isNotBlank() || selectedText.isNotBlank()) {
                        {
                            openNoteDialog(
                                sourceText = translationSourceText.ifBlank { selectedText },
                                knownTranslation = text.takeUnless { translationLoading || translationIsError },
                            )
                        }
                    } else {
                        null
                    },
                    onClose = {
                        translationText = null
                        translationSourceText = ""
                        translationLoading = false
                        translationIsError = false
                        if (selectedText.isNotBlank()) selectionTipVisible = true
                    },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            ReaderPageSurface(
                bookTitle = book.title,
                chapterTitle = currentChapterTitle,
                page = page,
                pageCount = pages.size,
                settings = settings,
                selectionRange = selectionRange,
                wordSelectionRange = wordSelectionRange,
                modifier = Modifier.fillMaxSize(),
                onPreviousPage = { goPreviousPage() },
                onNextPage = { goNextPage() },
                onBack = onBack,
                onOpenToc = { tocOpen = true },
                onOpenPageSettings = { pageSettingsOpen = true },
                onWordLongPress = { word, paragraphIndex, wordRange ->
                    selectionStart = null
                    selectionEnd = null
                    wordSelectionRange = wordRange
                    lookupWord(word, paragraphIndex, replaceStack = true)
                },
                onSelectionChange = { start, end ->
                    selectionStart = start
                    selectionEnd = end
                    selectionTipVisible = true
                    clearWordLookup()
                },
                selectionTipVisible = selectionTipVisible,
                onSelectionTap = { selectionTipVisible = true },
                selectedText = selectedText,
                onTranslateSelection = {
                    if (selectedText.isNotBlank()) {
                        translationText = "查阅中..."
                        translationSourceText = selectedText
                        translationLoading = true
                        translationIsError = false
                        selectionTipVisible = false
                        clearWordLookup()
                        scope.launch {
                            val result = runCatching {
                                OpenAiChatTranslator.translate(selectedText, settings.translation)
                            }
                            result.onSuccess {
                                translationText = it
                                translationIsError = false
                                onAddLookupHistory(
                                    page.paragraphIndexForDisplayOffset(selectionRange?.first ?: 0),
                                    LookupHistoryType.TRANSLATION,
                                    selectedText,
                                    it,
                                    "",
                                )
                            }.onFailure {
                                translationText = it.message ?: "翻译失败"
                                translationIsError = true
                            }
                            translationLoading = false
                        }
                    }
                },
                onHighlightSelection = {
                    if (selectedText.isNotBlank()) {
                        onAddNote(
                            page.paragraphIndexForDisplayOffset(selectionRange?.first ?: 0),
                            selectedText,
                            "",
                            "",
                        )
                        selectionTipVisible = true
                    }
                },
                onNoteSelection = {
                    if (selectedText.isNotBlank()) {
                        selectionTipVisible = false
                        openNoteDialog(selectedText)
                    }
                },
                onChatSelection = {
                    if (selectedText.isNotBlank()) {
                        chatSelectionText = selectedText
                        chatSelectionParagraphIndex = page.paragraphIndexForDisplayOffset(selectionRange?.first ?: 0)
                        selectionTipVisible = false
                    }
                },
                onClearSelection = {
                    selectionStart = null
                    selectionEnd = null
                    selectionTipVisible = false
                },
            )
            if (wordStack.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(wordStack.size) {
                            detectTapGestures(onTap = { closeTopWordCard() })
                        },
                )
            }
        }
    }

    if (tocOpen) {
        TableOfContentsDialog(
            items = tocItems,
            onDismiss = { tocOpen = false },
            onJumpToStart = { item ->
                tocOpen = false
                jumpToParagraph(item.chapter.paragraphIndex)
            },
            onContinue = { item ->
                tocOpen = false
                item.progressAnchor?.let { anchor -> goToAnchor(anchor) }
            },
        )
    }

    if (pageSettingsOpen) {
        PageSettingsSheet(
            settings = settings,
            onChange = onSettingsChange,
            onDismiss = { pageSettingsOpen = false },
        )
    }

    noteSelectionText?.let { text ->
        SentenceNoteDialog(
            paragraph = text,
            noteFont = settings.noteFont,
            translationText = noteTranslationText,
            translationLoading = noteTranslationLoading,
            translationIsError = noteTranslationIsError,
            onDismiss = {
                noteSelectionText = null
                noteTranslationText = ""
                noteTranslationLoading = false
                noteTranslationIsError = false
                if (selectedText.isNotBlank()) selectionTipVisible = true
            },
            onSave = { sentences, translationText, noteText ->
                sentences.forEach { sentence ->
                    onAddNote(
                        page.paragraphIndexForDisplayOffset(selectionRange?.first ?: 0),
                        sentence,
                        translationText,
                        noteText,
                    )
                }
                noteSelectionText = null
                noteTranslationText = ""
                noteTranslationLoading = false
                noteTranslationIsError = false
                if (selectedText.isNotBlank()) selectionTipVisible = true
            },
        )
    }

    chatSelectionText?.let { text ->
        SelectionChatDialog(
            paragraph = text,
            bookTitle = book.title,
            settings = settings,
            onDismiss = {
                chatSelectionText = null
                if (selectedText.isNotBlank()) selectionTipVisible = true
            },
            onSend = { question ->
                onChatSelection(chatSelectionParagraphIndex, text, question)
                chatSelectionText = null
                if (selectedText.isNotBlank()) selectionTipVisible = true
            },
        )
    }
}

@Composable
private fun ReaderPageSurface(
    bookTitle: String,
    chapterTitle: String,
    page: ReaderPage,
    pageCount: Int,
    settings: ReaderSettings,
    selectionRange: IntRange?,
    wordSelectionRange: IntRange?,
    selectedText: String,
    selectionTipVisible: Boolean,
    modifier: Modifier = Modifier,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onBack: () -> Unit,
    onOpenToc: () -> Unit,
    onOpenPageSettings: () -> Unit,
    onWordLongPress: (String, Int, IntRange?) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onSelectionTap: () -> Unit,
    onTranslateSelection: () -> Unit,
    onHighlightSelection: () -> Unit,
    onNoteSelection: () -> Unit,
    onChatSelection: () -> Unit,
    onClearSelection: () -> Unit,
) {
    var layoutResult by remember(page.text) { mutableStateOf<TextLayoutResult?>(null) }
    var controlsVisible by remember { mutableStateOf(false) }
    var controlsVisibleEpoch by remember { mutableStateOf(0) }
    val titleTapHeight = with(LocalDensity.current) { 52.dp.toPx() }
    val fontFamily = settings.font.toFontFamily()
    val annotatedText = readerPageAnnotatedString(
        page = page,
        settings = settings,
        selectionRange = selectionRange,
        wordSelectionRange = wordSelectionRange,
        selectionColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
        wordSelectionColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.26f),
    )

    fun showControls() {
        controlsVisible = true
        controlsVisibleEpoch += 1
    }

    fun hideControls() {
        controlsVisible = false
    }

    LaunchedEffect(controlsVisible, controlsVisibleEpoch) {
        if (controlsVisible) {
            delay(3_000)
            controlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(page.index, pageCount, titleTapHeight, selectedText.isNotBlank()) {
                detectReaderPageTapGestures(
                    titleTapHeight = titleTapHeight,
                    hasSelection = selectedText.isNotBlank(),
                    onClearSelection = onClearSelection,
                    onPreviousPage = onPreviousPage,
                    onNextPage = onNextPage,
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 34.dp)
                    .pointerInput(bookTitle, chapterTitle, page.index, controlsVisible) {
                        detectTapGestures(
                            onTap = {
                                if (controlsVisible) hideControls() else showControls()
                            },
                            onLongPress = { onOpenPageSettings() },
                        )
                    },
            ) {
                ReaderCornerMeta(
                    bookTitle = bookTitle,
                    chapterTitle = chapterTitle,
                    pageIndex = page.index,
                    pageCount = pageCount,
                )
            }
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = fontFamily,
                        fontSize = settings.fontSizeSp.sp,
                        lineHeight = (settings.fontSizeSp * 1.72f).sp,
                        textAlign = TextAlign.Start,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(page.text) {
                            detectReaderTextGestures(
                                text = page.text,
                                getLayoutResult = { layoutResult },
                                canLookupWord = { selectedText.isBlank() },
                                currentSelectionRange = { selectionRange },
                                onWordLongPress = onWordLongPress,
                                paragraphIndexForOffset = { page.paragraphIndexForDisplayOffset(it) },
                                onSelectionTap = onSelectionTap,
                                onSelectionGestureStart = {},
                                onSelectionChange = onSelectionChange,
                                onSelectionGestureEnd = {},
                            )
                        },
                    onTextLayout = { layoutResult = it },
                )

                SelectionHandles(
                    text = page.text,
                    layoutResult = layoutResult,
                    selectionRange = selectionRange,
                    onSelectionChange = onSelectionChange,
                )

                if (selectedText.isNotBlank() && selectionTipVisible) {
                    SelectionTip(
                        layoutResult = layoutResult,
                        selectionRange = selectionRange,
                        onTranslate = onTranslateSelection,
                        onHighlight = onHighlightSelection,
                        onAddNote = onNoteSelection,
                        onChat = onChatSelection,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

        }

        if (controlsVisible) {
            ReaderTopControls(
                title = bookTitle,
                onBack = onBack,
                onOpenToc = onOpenToc,
                onOpenPageSettings = onOpenPageSettings,
                onHideControls = { hideControls() },
            )
        }
    }
}

private fun estimateReaderPageCharBudget(
    screenWidthDp: Int,
    screenHeightDp: Int,
    fontSizeSp: Int,
): Int {
    val textWidth = (screenWidthDp - 44).coerceAtLeast(220)
    val lineHeight = fontSizeSp * 1.72f
    val availableHeight = (screenHeightDp - 132).coerceAtLeast(360)
    val estimatedLines = (availableHeight / lineHeight).toInt().coerceAtLeast(8)
    val estimatedCharsPerLine = (textWidth / (fontSizeSp * 0.56f)).toInt().coerceAtLeast(18)
    return (estimatedLines * estimatedCharsPerLine * 0.82f).toInt().coerceIn(360, 780)
}

@Composable
private fun SelectionHandles(
    text: String,
    layoutResult: TextLayoutResult?,
    selectionRange: IntRange?,
    onSelectionChange: (Int, Int) -> Unit,
) {
    val layout = layoutResult ?: return
    val range = selectionRange ?: return
    if (text.isBlank()) return

    val startOffset = range.first.coerceIn(0, text.lastIndex)
    val endInclusive = range.last.coerceIn(startOffset, text.lastIndex)
    val endExclusive = (endInclusive + 1).coerceIn(0, text.length)
    val startRect = layout.getCursorRect(startOffset)
    val endRect = layout.getCursorRect(endExclusive)
    val startCursor = Offset(startRect.left, startRect.top)
    val endCursor = Offset(endRect.left, endRect.top)

    SelectionHandle(
        parentPosition = Offset(startRect.left, startRect.top),
        cursorPositionInText = startCursor,
        cursorHeightPx = (startRect.bottom - startRect.top).coerceAtLeast(1f),
        layoutResult = layout,
        fixedOffset = endInclusive,
        isStart = true,
        onSelectionChange = onSelectionChange,
    )
    SelectionHandle(
        parentPosition = Offset(endRect.left, endRect.top),
        cursorPositionInText = endCursor,
        cursorHeightPx = (endRect.bottom - endRect.top).coerceAtLeast(1f),
        layoutResult = layout,
        fixedOffset = range.first,
        isStart = false,
        onSelectionChange = onSelectionChange,
    )
}

@Composable
private fun SelectionHandle(
    parentPosition: Offset,
    cursorPositionInText: Offset,
    cursorHeightPx: Float,
    layoutResult: TextLayoutResult,
    fixedOffset: Int,
    isStart: Boolean,
    onSelectionChange: (Int, Int) -> Unit,
) {
    val density = LocalDensity.current
    val handleWidth = 34.dp
    val handleWidthPx = with(density) { handleWidth.toPx() }
    val horizontalInset = handleWidthPx / 2f
    val cursorHeight = with(density) { cursorHeightPx.toDp() }
    val primary = MaterialTheme.colorScheme.primary
    val currentLayoutResult by rememberUpdatedState(layoutResult)
    val currentFixedOffset by rememberUpdatedState(fixedOffset)
    val currentCursorPosition by rememberUpdatedState(cursorPositionInText)
    val currentHorizontalInset by rememberUpdatedState(horizontalInset)
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (parentPosition.x - horizontalInset).roundToInt(),
                    y = parentPosition.y.roundToInt(),
                )
            }
            .width(handleWidth)
            .height(cursorHeight + 16.dp)
            .pointerInput(isStart) {
                var dragPosition = cursorPositionInText
                detectDragGestures(
                    onDragStart = { start ->
                        val cursor = currentCursorPosition
                        dragPosition = Offset(
                            x = cursor.x - currentHorizontalInset + start.x,
                            y = cursor.y + start.y,
                        )
                    },
                    onDrag = { change, dragAmount ->
                        val activeLayout = currentLayoutResult
                        val activeFixedOffset = currentFixedOffset
                        dragPosition += dragAmount
                        val maxOffset = (activeLayout.layoutInput.text.length - 1).coerceAtLeast(0)
                        val offset = activeLayout.getOffsetForPosition(dragPosition).coerceIn(0, maxOffset)
                        if (isStart) {
                            onSelectionChange(offset.coerceIn(0, activeFixedOffset), activeFixedOffset)
                        } else {
                            val minEnd = (activeFixedOffset + 1).coerceAtMost(maxOffset)
                            onSelectionChange(activeFixedOffset, (offset - 1).coerceIn(minEnd, maxOffset))
                        }
                        change.consume()
                    },
                )
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(cursorHeight)
                    .clip(RoundedCornerShape(99.dp))
                    .background(primary),
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(primary),
            )
        }
    }
}

private suspend fun PointerInputScope.detectReaderPageTapGestures(
    titleTapHeight: Float,
    hasSelection: Boolean,
    onClearSelection: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        val up = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            waitForUpOrCancellation()
        } ?: return@awaitEachGesture
        if (up.isConsumed) return@awaitEachGesture
        val position = up.position
        when {
            position.y <= titleTapHeight -> Unit
            hasSelection -> {
                up.consume()
                onClearSelection()
            }
            position.x < size.width * 0.28f -> onPreviousPage()
            position.x > size.width * 0.72f -> onNextPage()
        }
    }
}

private suspend fun PointerInputScope.detectReaderTextGestures(
    text: String,
    getLayoutResult: () -> TextLayoutResult?,
    canLookupWord: () -> Boolean,
    currentSelectionRange: () -> IntRange?,
    onWordLongPress: (String, Int, IntRange?) -> Unit,
    paragraphIndexForOffset: (Int) -> Int,
    onSelectionTap: () -> Unit,
    onSelectionGestureStart: () -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onSelectionGestureEnd: () -> Unit,
) {
    awaitEachGesture {
        val firstDown = awaitFirstDown(requireUnconsumed = false)
        val layout = getLayoutResult() ?: return@awaitEachGesture
        val longPressOffset = layout.getOffsetForPosition(firstDown.position)
        val word = extractWordAt(text, longPressOffset)
        val wordRange = wordRangeAt(text, longPressOffset)
        val paragraphRange = paragraphRangeAt(text, longPressOffset)
        val sentenceRange = sentenceRangeAt(text, longPressOffset) ?: paragraphRange
        val pointerId = firstDown.id
        var selecting = false
        var wordLookupTriggered = false
        var pointerUp = false

        try {
            currentSelectionRange()?.let { activeRange ->
                val up = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                    waitForUpOrCancellation()
                } ?: return@awaitEachGesture
                if (!up.isConsumed) {
                    val tapOffset = layout.getOffsetForPosition(up.position)
                    if (tapOffset in activeRange) {
                        firstDown.consume()
                        up.consume()
                        onSelectionTap()
                    }
                }
                return@awaitEachGesture
            }
            val wordLookupDelayMillis = 700L
            var releasedBeforeLookup = false
            withTimeoutOrNull(wordLookupDelayMillis) {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == pointerId }
                        ?: event.changes.firstOrNull()
                    if (change == null) continue
                    if (change.changedToUpIgnoreConsumed() || !change.pressed) {
                        pointerUp = true
                        releasedBeforeLookup = true
                        return@withTimeoutOrNull
                    }
                    if (change.positionChangedIgnoreConsumed()) {
                        val movedEnough = (change.position - firstDown.position).getDistance() >
                            viewConfiguration.touchSlop
                        if (!movedEnough || sentenceRange == null) continue
                        selecting = true
                        firstDown.consume()
                        change.consume()
                        onSelectionGestureStart()
                        onSelectionChange(sentenceRange.first, sentenceRange.last)
                        val currentOffset = (getLayoutResult() ?: layout).getOffsetForPosition(change.position)
                        val anchorOffset = if (currentOffset < sentenceRange.first) {
                            sentenceRange.last
                        } else {
                            sentenceRange.first
                        }
                        onSelectionChange(anchorOffset, currentOffset)
                        return@withTimeoutOrNull
                    }
                }
            }
            if (releasedBeforeLookup || pointerUp) {
                return@awaitEachGesture
            }
            if (!selecting && word != null && canLookupWord()) {
                firstDown.consume()
                wordLookupTriggered = true
                onWordLongPress(word, paragraphIndexForOffset(longPressOffset), wordRange)
            }

            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId }
                    ?: event.changes.firstOrNull()
                    ?: continue
                if (change.changedToUpIgnoreConsumed() || !change.pressed) {
                    if (wordLookupTriggered || selecting) change.consume()
                    break
                }
                if (wordLookupTriggered) {
                    change.consume()
                    continue
                }
                    if (change.positionChangedIgnoreConsumed()) {
                        val currentLayout = getLayoutResult() ?: continue
                        if (!selecting) {
                            val movedEnough = (change.position - firstDown.position).getDistance() >
                                viewConfiguration.touchSlop
                            if (!movedEnough || sentenceRange == null) continue
                            selecting = true
                            onSelectionGestureStart()
                            onSelectionChange(sentenceRange.first, sentenceRange.last)
                        }
                        val currentOffset = currentLayout.getOffsetForPosition(change.position)
                        val activeSentenceRange = sentenceRange ?: continue
                        val anchorOffset = if (currentOffset < activeSentenceRange.first) {
                            activeSentenceRange.last
                        } else {
                            activeSentenceRange.first
                        }
                        onSelectionChange(anchorOffset, currentOffset)
                    change.consume()
                }
            }
        } finally {
            if (selecting) onSelectionGestureEnd()
        }
    }
}

private fun paragraphRangeAt(text: String, offset: Int): IntRange? {
    if (text.isBlank()) return null
    val anchor = offset.coerceIn(0, text.lastIndex)
    val previousBreak = text.lastIndexOf("\n\n", startIndex = anchor)
    val nextBreak = text.indexOf("\n\n", startIndex = anchor)
    var start = if (previousBreak >= 0) previousBreak + 2 else 0
    var end = if (nextBreak >= 0) nextBreak else text.length
    while (start < end && text[start].isWhitespace()) start += 1
    while (end > start && text[end - 1].isWhitespace()) end -= 1
    if (end - start < 2) return null
    return start..(end - 1)
}

private fun sentenceRangeAt(text: String, offset: Int): IntRange? {
    val paragraphRange = paragraphRangeAt(text, offset) ?: return null
    val anchor = offset.coerceIn(paragraphRange.first, paragraphRange.last)
    var start = paragraphRange.first
    var index = anchor - 1
    while (index >= paragraphRange.first) {
        if (text[index].isSentenceTerminator()) {
            start = index + 1
            break
        }
        index -= 1
    }
    var end = paragraphRange.last
    index = anchor
    while (index <= paragraphRange.last) {
        if (text[index].isSentenceTerminator()) {
            end = index
            while (end + 1 <= paragraphRange.last && text[end + 1].isClosingSentencePunctuation()) {
                end += 1
            }
            break
        }
        index += 1
    }
    while (start <= end && text[start].isWhitespace()) start += 1
    while (end >= start && text[end].isWhitespace()) end -= 1
    return (start..end).takeIf { end - start >= 1 }
}

private fun Char.isSentenceTerminator(): Boolean =
    this == '.' || this == '?' || this == '!' || this == ';' || this == '。' || this == '？' || this == '！'

private fun Char.isClosingSentencePunctuation(): Boolean =
    this == '"' || this == '\'' || this == '”' || this == '’' || this == ')' || this == ']' || this == '}'

private fun wordRangeAt(text: String, offset: Int): IntRange? {
    if (text.isBlank() || offset !in text.indices) return null
    val anchor = when {
        text[offset].isReaderWordChar() -> offset
        offset > 0 && text[offset - 1].isReaderWordChar() -> offset - 1
        else -> return null
    }
    var start = anchor
    var end = anchor
    while (start > 0 && text[start - 1].isReaderWordChar()) start -= 1
    while (end + 1 < text.length && text[end + 1].isReaderWordChar()) end += 1
    while (start <= end && (text[start] == '\'' || text[start] == '-')) start += 1
    while (end >= start && (text[end] == '\'' || text[end] == '-')) end -= 1
    if (start > end) return null
    return (start..end).takeIf { range -> text.substring(range).any { it.isReaderEnglishLetter() } }
}

private fun Char.isReaderWordChar(): Boolean =
    isReaderEnglishLetter() || this == '\'' || this == '-'

private fun Char.isReaderEnglishLetter(): Boolean =
    this in 'a'..'z' || this in 'A'..'Z'

private fun ReaderPage.paragraphIndexForDisplayOffset(offset: Int): Int {
    if (paragraphs.isEmpty()) return firstParagraphIndex
    val anchor = offset.coerceAtLeast(0)
    var cursor = 0
    paragraphs.forEach { paragraph ->
        val start = cursor
        val endExclusive = start + paragraph.text.length
        if (anchor <= endExclusive) return paragraph.paragraphIndex
        cursor = endExclusive + 2
    }
    return paragraphs.last().paragraphIndex
}

@Composable
private fun ReaderCornerMeta(
    bookTitle: String,
    chapterTitle: String,
    pageIndex: Int,
    pageCount: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = bookTitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$chapterTitle · ${pageIndex + 1}/$pageCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReaderTopControls(
    title: String,
    onBack: () -> Unit,
    onOpenToc: () -> Unit,
    onOpenPageSettings: () -> Unit,
    onHideControls: () -> Unit,
) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(title) {
                        detectTapGestures(
                            onTap = { onHideControls() },
                            onLongPress = { onOpenPageSettings() },
                        )
                    },
            )
            IconButton(onClick = onOpenToc) {
                Icon(Icons.Filled.AutoStories, contentDescription = "目录")
            }
        }
    }
}

@Composable
private fun TableOfContentsDialog(
    items: List<ChapterTocItem>,
    onDismiss: () -> Unit,
    onJumpToStart: (ChapterTocItem) -> Unit,
    onContinue: (ChapterTocItem) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.AutoStories, contentDescription = null) },
        title = { Text("目录") },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(items) { _, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onJumpToStart(item) }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${item.index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(34.dp),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = item.chapter.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = buildString {
                                    append("第 ${item.startPageIndex + 1} 页开始")
                                    item.progressPageIndex?.let { progressPageIndex ->
                                        append(" · 已读到第 ${progressPageIndex + 1} 页")
                                    } ?: append(" · 未开始")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        TextButton(
                            onClick = { onContinue(item) },
                            enabled = item.progressAnchor != null,
                        ) {
                            Text("继续读")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageSettingsSheet(
    settings: ReaderSettings,
    onChange: (ReaderSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("页面设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            FontSelectionSection(
                title = "正文字体",
                selectedFont = settings.font,
                onSelect = { onChange(settings.copy(font = it)) },
            )
            HorizontalDivider()
            Text("字号 ${settings.fontSizeSp}sp", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Slider(
                value = settings.fontSizeSp.toFloat(),
                onValueChange = { onChange(settings.copy(fontSizeSp = it.toInt().coerceIn(14, 30))) },
                valueRange = 14f..30f,
                steps = 15,
            )
            HorizontalDivider()
            Text("主题", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            ReaderTheme.entries.forEach { theme ->
                SettingRadioRow(
                    label = theme.label,
                    selected = settings.theme == theme,
                    onClick = { onChange(settings.copy(theme = theme)) },
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun readerPageAnnotatedString(
    page: ReaderPage,
    settings: ReaderSettings,
    selectionRange: IntRange?,
    wordSelectionRange: IntRange?,
    selectionColor: Color,
    wordSelectionColor: Color,
): AnnotatedString {
    val text = page.text
    return remember(
        text,
        settings.fontSizeSp,
        selectionRange,
        wordSelectionRange,
        selectionColor,
        wordSelectionColor,
    ) {
        buildAnnotatedString {
            append(text)
            wordSelectionRange?.let { range ->
                addStyle(
                    SpanStyle(background = wordSelectionColor),
                    range.first.coerceIn(0, text.length),
                    (range.last + 1).coerceIn(0, text.length),
                )
            }
            selectionRange?.let { range ->
                addStyle(
                    SpanStyle(background = selectionColor),
                    range.first.coerceIn(0, text.length),
                    (range.last + 1).coerceIn(0, text.length),
                )
            }
            chapterDropInitialOffsets(page).forEach { offset ->
                if (offset in text.indices) {
                    addStyle(
                        SpanStyle(
                            fontSize = (settings.fontSizeSp * 2).sp,
                            fontWeight = FontWeight.Black,
                        ),
                        offset,
                        offset + 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionTip(
    layoutResult: TextLayoutResult?,
    selectionRange: IntRange?,
    onTranslate: () -> Unit,
    onHighlight: () -> Unit,
    onAddNote: () -> Unit,
    onChat: () -> Unit,
) {
    val layout = layoutResult ?: return
    val range = selectionRange ?: return
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val tipWidth = 276.dp
    val tipWidthPx = with(density) { tipWidth.toPx() }
    val horizontalMarginPx = with(density) { 12.dp.toPx() }
    val verticalGapPx = with(density) { 10.dp.toPx() }
    val tipHeightPx = with(density) { 48.dp.toPx() }
    val startOffset = range.first.coerceIn(0, layout.layoutInput.text.length)
    val startLine = layout.getLineForOffset(startOffset)
    val lineLeft = layout.getLineLeft(startLine)
    val lineRight = layout.getLineRight(startLine)
    val lineTop = layout.getLineTop(startLine)
    val lineBottom = layout.getLineBottom(startLine)
    val maxX = with(density) { configuration.screenWidthDp.dp.toPx() } - tipWidthPx - horizontalMarginPx
    val x = ((lineLeft + lineRight) / 2f - tipWidthPx / 2f)
        .coerceIn(horizontalMarginPx, maxX.coerceAtLeast(horizontalMarginPx))
    val preferredY = lineTop - tipHeightPx - verticalGapPx
    val y = if (preferredY >= 0f) preferredY else lineBottom + verticalGapPx
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        modifier = Modifier
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .width(tipWidth),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectionTipButton(
                text = "翻译",
                icon = { Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(16.dp)) },
                onClick = onTranslate,
            )
            SelectionTipButton(
                text = "划线",
                icon = { Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                onClick = onHighlight,
            )
            SelectionTipButton(
                text = "摘句",
                icon = { Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp)) },
                onClick = onAddNote,
            )
            SelectionTipButton(
                text = "对话",
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp)) },
                onClick = onChat,
            )
        }
    }
}

@Composable
private fun SelectionTipButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
        modifier = Modifier.width(67.dp),
    ) {
        icon()
        Spacer(Modifier.width(3.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun PageTurnBar(
    pageIndex: Int,
    pageCount: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onPreviousPage, enabled = pageIndex > 0) {
            Text("上一页")
        }
        Text(
            text = "${pageIndex + 1} / $pageCount",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onNextPage, enabled = pageIndex < pageCount - 1) {
            Text("下一页")
        }
    }
}

@Composable
private fun MissingBookScreen(
    modifier: Modifier,
    onBack: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("找不到这本书", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = onBack, shape = RoundedCornerShape(8.dp)) {
                Text("回到书架")
            }
        }
    }
}

@Composable
private fun WordLookupPanel(
    entry: WordEntry,
    stackDepth: Int,
    ttsAccent: TtsAccent,
    onClose: () -> Unit,
    onLookupWord: (String) -> Unit,
    onTtsAccentChange: (TtsAccent) -> Unit,
    onSpeak: () -> Unit,
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.toFloat().coerceAtLeast(1f)
    val screenHeightPx = with(LocalDensity.current) {
        screenHeightDp.dp.toPx().coerceAtLeast(1f)
    }
    val minPanelFraction = 0.38f
    val maxPanelFraction = ((screenHeightDp - 112f) / screenHeightDp).coerceIn(minPanelFraction, 0.88f)
    var panelFraction by rememberSaveable(entry.word) { mutableStateOf(minPanelFraction) }
    LaunchedEffect(maxPanelFraction) {
        panelFraction = panelFraction.coerceIn(minPanelFraction, maxPanelFraction)
    }
    val panelHeight = (screenHeightDp * panelFraction).dp
    val nextAccent = if (ttsAccent == TtsAccent.US) TtsAccent.UK else TtsAccent.US
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .pointerInput(maxPanelFraction, screenHeightPx) {
                        detectDragGestures { change, dragAmount ->
                            panelFraction = (panelFraction - dragAmount.y / screenHeightPx)
                                .coerceIn(minPanelFraction, maxPanelFraction)
                            change.consume()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    LookupText(
                        text = entry.word,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        onLookupWord = onLookupWord,
                    )
                    Text(
                        text = buildString {
                            append("美 ").append(entry.usIpa())
                            append(" · 英 ").append(entry.ukIpa())
                            if (stackDepth > 1) append(" · $stackDepth 层")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = { onTtsAccentChange(nextAccent) },
                    label = { Text("${ttsAccent.label} · 切换") },
                )
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "播放读音")
                }
            }
            HorizontalDivider()
            WordInfoSection(
                title = "释义",
                text = entry.meaning,
                onLookupWord = onLookupWord,
            )
            if (entry.root.isNotBlank()) {
                WordInfoSection(
                    title = "词根",
                    text = entry.root,
                    onLookupWord = onLookupWord,
                )
            } else if (entry.detailsLoading) {
                WordInfoSkeleton(title = "词根")
            }
            if (entry.cognates.isNotEmpty()) {
                WordListSection(
                    title = "同源词",
                    words = entry.cognates,
                    onLookupWord = onLookupWord,
                )
            } else if (entry.detailsLoading) {
                WordChipListSkeleton(title = "同源词")
            }
            if (entry.synonyms.isNotEmpty()) {
                WordListSection(
                    title = "近义词",
                    words = entry.synonyms,
                    onLookupWord = onLookupWord,
                )
            } else if (entry.detailsLoading) {
                WordChipListSkeleton(title = "近义词")
            }
        }
    }
}

@Composable
private fun ShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-progress",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    val highlight = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val start = -260f + progress * 520f
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(start, 0f),
        end = Offset(start + 260f, 120f),
    )
}

@Composable
private fun WordInfoSkeleton(title: String) {
    val brush = ShimmerBrush()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        WordSectionTitle(title)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.64f)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(brush),
        )
    }
}

@Composable
private fun WordChipListSkeleton(title: String) {
    val brush = ShimmerBrush()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        WordSectionTitle(title)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(58.dp, 72.dp, 64.dp).forEach { width ->
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush),
                )
            }
        }
    }
}

@Composable
private fun WordSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun WordInfoSection(
    title: String,
    text: String,
    onLookupWord: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        WordSectionTitle(title)
        LookupText(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            onLookupWord = onLookupWord,
        )
    }
}

@Composable
private fun WordListSection(
    title: String,
    words: List<String>,
    onLookupWord: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        WordSectionTitle(title)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            words.take(6).forEach { word ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        LookupText(
                            text = word,
                            style = MaterialTheme.typography.labelLarge,
                            onLookupWord = onLookupWord,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LookupText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onLookupWord: (String) -> Unit,
) {
    var layoutResult by remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        style = style,
        fontWeight = fontWeight,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier.pointerInput(text) {
            detectTapGestures(
                onLongPress = { position ->
                    val offset = layoutResult?.getOffsetForPosition(position) ?: return@detectTapGestures
                    val word = extractWordAt(text, offset) ?: return@detectTapGestures
                    onLookupWord(word)
                },
            )
        },
        onTextLayout = { layoutResult = it },
    )
}

@Composable
private fun TranslationResultPanel(
    sourceText: String,
    text: String,
    loading: Boolean,
    isError: Boolean,
    onAddNote: (() -> Unit)?,
    onClose: () -> Unit,
) {
    val showComparison = sourceText.isNotBlank() && LocalConfiguration.current.screenWidthDp >= 600
    val contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(22.dp))
            }
            if (showComparison) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("原文", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sourceText, style = MaterialTheme.typography.bodyMedium, color = contentColor)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("译文", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text, style = MaterialTheme.typography.bodyMedium, color = contentColor)
                    }
                }
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    modifier = Modifier.weight(1f),
                )
            }
            if (onAddNote != null) {
                IconButton(
                    onClick = onAddNote,
                    enabled = !loading && !isError,
                ) {
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = "摘句")
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "关闭译文")
            }
        }
    }
}

@Composable
private fun ReaderSettingsDialog(
    settings: ReaderSettings,
    onChange: (ReaderSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Palette, contentDescription = null) },
        title = { Text("阅读设置") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                FontSelectionSection(
                    title = "正文字体",
                    selectedFont = settings.font,
                    onSelect = { onChange(settings.copy(font = it)) },
                )
                HorizontalDivider()
                Text("字号 ${settings.fontSizeSp}sp", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Slider(
                    value = settings.fontSizeSp.toFloat(),
                    onValueChange = { onChange(settings.copy(fontSizeSp = it.toInt().coerceIn(14, 30))) },
                    valueRange = 14f..30f,
                    steps = 15,
                )
                HorizontalDivider()
                Text("主题", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                ReaderTheme.entries.forEach { theme ->
                    SettingRadioRow(
                        label = theme.label,
                        selected = settings.theme == theme,
                        onClick = { onChange(settings.copy(theme = theme)) },
                    )
                }
                HorizontalDivider()
                TranslationSettingsSection(
                    settings = settings,
                    onChange = onChange,
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("完成")
            }
        },
    )
}

@Composable
private fun TranslationSettingsSection(
    settings: ReaderSettings,
    onChange: (ReaderSettings) -> Unit,
) {
    val translation = settings.translation
    val inputColors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.background,
        unfocusedContainerColor = MaterialTheme.colorScheme.background,
        disabledContainerColor = MaterialTheme.colorScheme.background,
        errorContainerColor = MaterialTheme.colorScheme.background,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
    )
    Text("OpenAI 翻译", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    TextField(
        value = translation.baseUrl,
        onValueChange = { value ->
            onChange(settings.copy(translation = translation.copy(baseUrl = value.trim())))
        },
        label = { Text("Base URL") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        modifier = Modifier.fillMaxWidth(),
        colors = inputColors,
    )
    TextField(
        value = translation.apiKey,
        onValueChange = { value ->
            onChange(settings.copy(translation = translation.copy(apiKey = value.trim())))
        },
        label = { Text("API Key") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        colors = inputColors,
    )
    TextField(
        value = translation.model,
        onValueChange = { value ->
            onChange(settings.copy(translation = translation.copy(model = value.trim())))
        },
        label = { Text("模型") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = inputColors,
    )
    Text(
        text = "默认接口为 /chat/completions，兼容 OpenAI 和同协议网关。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SettingRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FontSelectionSection(
    title: String,
    selectedFont: ReaderFont,
    onSelect: (ReaderFont) -> Unit,
) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    ReaderFont.entries.forEach { font ->
        FontOptionRow(
            font = font,
            selected = selectedFont == font,
            onClick = { onSelect(font) },
        )
    }
}

@Composable
private fun FontOptionRow(
    font: ReaderFont,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = font.label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = font.toFontFamily()),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = font.categoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = "The quick brown fox jumps over a lazy dog.",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = font.toFontFamily()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    settings: ReaderSettings,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    onSettingsChange: (ReaderSettings) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text("API 配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TranslationSettingsSection(settings = settings, onChange = onSettingsChange)
                    }
                }
            }
            item {
                Text("笔记字体", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            }
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FontSelectionSection(
                            title = "笔记字体",
                            selectedFont = settings.noteFont,
                            onSelect = { onSettingsChange(settings.copy(noteFont = it)) },
                        )
                    }
                }
            }
            item {
                Text(
                    text = "页面字体、字号和主题放在阅读页：长按顶部展开后的书名打开页面设置。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SentenceNoteDialog(
    paragraph: String,
    noteFont: ReaderFont,
    translationText: String,
    translationLoading: Boolean,
    translationIsError: Boolean,
    onDismiss: () -> Unit,
    onSave: (sentences: List<String>, translationText: String, noteText: String) -> Unit,
) {
    val excerpt = remember(paragraph) { paragraph.trim() }
    var noteText by remember(paragraph) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.BookmarkAdd, contentDescription = null) },
        title = { Text("加入笔记本") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = excerpt.ifBlank { "没有识别到可摘录的内容。" },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp),
                    )
                }
                Surface(
                    color = if (translationIsError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (translationLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = translationText.ifBlank { "暂无译文" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (translationIsError) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                TextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("自己的笔记") },
                    minLines = 3,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = noteFont.toFontFamily()),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        listOf(excerpt),
                        translationText.takeUnless { translationLoading || translationIsError }.orEmpty(),
                        noteText,
                    )
                },
                enabled = excerpt.isNotBlank() && !translationLoading,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun SelectionChatDialog(
    paragraph: String,
    bookTitle: String,
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
) {
    val excerpt = remember(paragraph) { paragraph.trim() }
    var question by remember(paragraph) { mutableStateOf("") }
    var suggestions by remember(paragraph) {
        mutableStateOf(selectionChatQuestionPrompts.randomThree())
    }
    var suggestionsLoading by remember(paragraph) { mutableStateOf(settings.translation.isConfigured) }
    val scope = rememberCoroutineScope()

    fun refreshSuggestions() {
        if (suggestionsLoading) return
        scope.launch {
            suggestionsLoading = true
            if (settings.translation.isConfigured) {
                val result = runCatching {
                    OpenAiBookChat.suggestSelectionQuestions(
                        bookTitle = bookTitle,
                        excerpt = excerpt,
                        settings = settings.translation,
                    )
                }
                suggestions = result.getOrDefault(selectionChatQuestionPrompts.randomThree())
                    .ifEmpty { selectionChatQuestionPrompts.randomThree() }
            } else {
                delay(360)
                suggestions = selectionChatQuestionPrompts.randomThree()
            }
            suggestionsLoading = false
        }
    }

    LaunchedEffect(paragraph, bookTitle, settings.translation.isConfigured) {
        if (settings.translation.isConfigured) {
            val result = runCatching {
                OpenAiBookChat.suggestSelectionQuestions(
                    bookTitle = bookTitle,
                    excerpt = excerpt,
                    settings = settings.translation,
                )
            }
            suggestions = result.getOrDefault(selectionChatQuestionPrompts.randomThree())
                .ifEmpty { selectionChatQuestionPrompts.randomThree() }
            suggestionsLoading = false
        } else {
            suggestionsLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
        title = { Text("和这段文字对话") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = excerpt.ifBlank { "没有识别到选中文本。" },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .heightIn(max = 180.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "猜你想问",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = { refreshSuggestions() }, enabled = !suggestionsLoading) {
                        Icon(Icons.Filled.Refresh, contentDescription = "换一批")
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (suggestionsLoading) {
                        val brush = ShimmerBrush()
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (index == 2) 0.84f else 1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(brush),
                            )
                        }
                    } else {
                        suggestions.forEach { prompt ->
                            AssistChip(
                                onClick = { question = prompt },
                                label = {
                                    Text(
                                        text = prompt,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                TextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("想问什么") },
                    placeholder = { Text("例如：这段话的重点是什么？") },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(question) },
                enabled = excerpt.isNotBlank() && question.trim().isNotBlank(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("发送")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

private enum class NotesDisplayMode {
    SUMMARY,
    ENGLISH,
    DETAIL,
}

private fun NotesDisplayMode.next(): NotesDisplayMode =
    when (this) {
        NotesDisplayMode.SUMMARY -> NotesDisplayMode.ENGLISH
        NotesDisplayMode.ENGLISH -> NotesDisplayMode.DETAIL
        NotesDisplayMode.DETAIL -> NotesDisplayMode.SUMMARY
    }

private val NotesDisplayMode.contentDescription: String
    get() = when (this) {
        NotesDisplayMode.SUMMARY -> "概要模式"
        NotesDisplayMode.ENGLISH -> "纯英文模式"
        NotesDisplayMode.DETAIL -> "详细模式"
    }

private enum class NotesFilter(val label: String) {
    ALL("全部"),
    NOTES("笔记"),
    LOOKUPS("查词"),
}

private sealed class NotesTimelineItem {
    abstract val id: String
    abstract val updatedAt: Long

    data class NoteItem(val note: ReaderNote) : NotesTimelineItem() {
        override val id: String = "note-${note.id}"
        override val updatedAt: Long = note.updatedAt
    }

    data class LookupItem(val item: LookupHistoryEntry) : NotesTimelineItem() {
        override val id: String = "history-${item.id}"
        override val updatedAt: Long = item.updatedAt
    }
}

private fun String.compactText(maxChars: Int): String {
    val normalized = replace(Regex("\\s+"), " ").trim()
    if (normalized.length <= maxChars) return normalized
    return normalized.take(maxChars).trimEnd() + "..."
}

private fun LookupHistoryEntry.summaryMeaning(): String =
    resultText
        .lineSequence()
        .firstOrNull { it.isNotBlank() }
        ?.compactText(78)
        .orEmpty()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesScreen(
    notes: List<ReaderNote>,
    lookupHistory: List<LookupHistoryEntry>,
    noteFont: ReaderFont,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    onExport: () -> Unit,
    onUpdateNote: (ReaderNote, String) -> Unit,
    onDeleteNote: (ReaderNote) -> Unit,
    onDeleteSelectedItems: (List<ReaderNote>, List<LookupHistoryEntry>) -> Unit,
    onDeleteLookupHistory: (List<LookupHistoryEntry>) -> Unit,
    onClearHistory: () -> Unit,
    onOpenSource: (String, Int) -> Unit,
) {
    var editingNote by remember { mutableStateOf<ReaderNote?>(null) }
    var deletingNote by remember { mutableStateOf<ReaderNote?>(null) }
    var batchDeleteConfirmOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var batchDeleteMode by remember { mutableStateOf(false) }
    var displayMode by remember { mutableStateOf(NotesDisplayMode.SUMMARY) }
    var filter by remember { mutableStateOf(NotesFilter.ALL) }
    val selectedItemIds = remember { mutableStateMapOf<String, Boolean>() }
    val expandedCards = remember { mutableStateMapOf<String, Boolean>() }
    val selectedNotes = notes.filter { selectedItemIds["note-${it.id}"] == true }
    val selectedLookupEntries = lookupHistory.filter { selectedItemIds["history-${it.id}"] == true }
    val selectedItemCount = selectedNotes.size + selectedLookupEntries.size
    val activeFilter = filter
    val timelineItems = remember(notes, lookupHistory, activeFilter) {
        buildList {
            if (activeFilter == NotesFilter.ALL || activeFilter == NotesFilter.NOTES) {
                notes.forEach { add(NotesTimelineItem.NoteItem(it)) }
            }
            if (activeFilter == NotesFilter.ALL || activeFilter == NotesFilter.LOOKUPS) {
                lookupHistory.forEach { add(NotesTimelineItem.LookupItem(it)) }
            }
        }.sortedByDescending { it.updatedAt }
    }

    LaunchedEffect(notes, lookupHistory) {
        selectedItemIds.keys.toList().forEach { id ->
            val stillExists = notes.any { id == "note-${it.id}" } || lookupHistory.any { id == "history-${it.id}" }
            if (!stillExists) selectedItemIds.remove(id)
        }
        if (notes.isEmpty() && lookupHistory.isEmpty()) batchDeleteMode = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (batchDeleteMode) "已选 ${selectedItemCount} 条" else "笔记",
                        fontWeight = FontWeight.Black,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            displayMode = displayMode.next()
                            expandedCards.clear()
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = displayMode.contentDescription)
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                        ) {
                            if (batchDeleteMode) {
                                DropdownMenuItem(
                                    text = { Text("删除所选") },
                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                    enabled = selectedItemCount > 0,
                                    onClick = {
                                        menuOpen = false
                                        batchDeleteConfirmOpen = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("取消批量") },
                                    leadingIcon = { Icon(Icons.Filled.Close, contentDescription = null) },
                                    onClick = {
                                        menuOpen = false
                                        selectedItemIds.clear()
                                        batchDeleteMode = false
                                    },
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("导出") },
                                    leadingIcon = { Icon(Icons.Filled.Download, contentDescription = null) },
                                    onClick = {
                                        menuOpen = false
                                        onExport()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("批量删除") },
                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                    enabled = notes.isNotEmpty() || lookupHistory.isNotEmpty(),
                                    onClick = {
                                        menuOpen = false
                                        selectedItemIds.clear()
                                        batchDeleteMode = true
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("清空查词") },
                                    leadingIcon = { Icon(Icons.Filled.History, contentDescription = null) },
                                    enabled = lookupHistory.isNotEmpty(),
                                    onClick = {
                                        menuOpen = false
                                        onClearHistory()
                                    },
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        if (notes.isEmpty() && lookupHistory.isEmpty()) {
            EmptyNotes(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    NotesFilterRow(
                        filter = activeFilter,
                        enabled = !batchDeleteMode,
                        onFilterChange = {
                            filter = it
                            expandedCards.clear()
                        },
                    )
                }
                if (timelineItems.isEmpty()) {
                    item {
                        EmptyFilteredNotes(filter = activeFilter)
                    }
                } else {
                    itemsIndexed(timelineItems, key = { _, item -> item.id }) { _, timelineItem ->
                        when (timelineItem) {
                            is NotesTimelineItem.NoteItem -> {
                                val note = timelineItem.note
                                NoteCard(
                                    note = note,
                                    noteFont = noteFont,
                                    displayMode = displayMode,
                                    expanded = expandedCards[timelineItem.id] == true,
                                    batchDeleteMode = batchDeleteMode,
                                    selected = selectedItemIds[timelineItem.id] == true,
                                    onToggleExpanded = {
                                        expandedCards[timelineItem.id] = expandedCards[timelineItem.id] != true
                                    },
                                    onSelectedChange = { selected ->
                                        if (selected) {
                                            selectedItemIds[timelineItem.id] = true
                                        } else {
                                            selectedItemIds.remove(timelineItem.id)
                                        }
                                    },
                                    onEdit = { editingNote = note },
                                    onOpenSource = { onOpenSource(note.bookId, note.paragraphIndex) },
                                    onDelete = { deletingNote = note },
                                )
                            }

                            is NotesTimelineItem.LookupItem -> {
                                LookupHistoryCard(
                                    item = timelineItem.item,
                                    displayMode = displayMode,
                                    expanded = expandedCards[timelineItem.id] == true,
                                    batchDeleteMode = batchDeleteMode,
                                    selected = selectedItemIds[timelineItem.id] == true,
                                    onToggleExpanded = {
                                        expandedCards[timelineItem.id] = expandedCards[timelineItem.id] != true
                                    },
                                    onSelectedChange = { selected ->
                                        if (selected) {
                                            selectedItemIds[timelineItem.id] = true
                                        } else {
                                            selectedItemIds.remove(timelineItem.id)
                                        }
                                    },
                                    onOpenSource = { onOpenSource(timelineItem.item.bookId, timelineItem.item.paragraphIndex) },
                                    onDelete = { onDeleteLookupHistory(listOf(timelineItem.item)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editingNote?.let { note ->
        EditNoteDialog(
            note = note,
            noteFont = noteFont,
            onDismiss = { editingNote = null },
            onSave = { nextText ->
                editingNote = null
                onUpdateNote(note, nextText)
            },
        )
    }

    deletingNote?.let { note ->
        AlertDialog(
            onDismissRequest = { deletingNote = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("删除这条笔记？") },
            text = { Text("删除后不会影响原书内容。") },
            confirmButton = {
                Button(
                    onClick = {
                        deletingNote = null
                        onDeleteNote(note)
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNote = null }) {
                    Text("取消")
                }
            },
        )
    }

    if (batchDeleteConfirmOpen) {
        AlertDialog(
            onDismissRequest = { batchDeleteConfirmOpen = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("删除所选记录？") },
            text = { Text("将删除 ${selectedItemCount} 条记录，不会影响原书内容。") },
            confirmButton = {
                Button(
                    onClick = {
                        batchDeleteConfirmOpen = false
                        onDeleteSelectedItems(selectedNotes, selectedLookupEntries)
                        selectedItemIds.clear()
                        batchDeleteMode = false
                    },
                    enabled = selectedItemCount > 0,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { batchDeleteConfirmOpen = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun NotesFilterRow(
    filter: NotesFilter,
    enabled: Boolean,
    onFilterChange: (NotesFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        NotesFilter.entries.forEach { option ->
            val selected = option == filter
            Surface(
                color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(7.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(7.dp))
                    .clickable(enabled = enabled && !selected) { onFilterChange(option) },
            ) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyFilteredNotes(filter: NotesFilter) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = when (filter) {
                NotesFilter.ALL -> "还没有记录"
                NotesFilter.NOTES -> "还没有摘句笔记"
                NotesFilter.LOOKUPS -> "还没有查词记录"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
private fun EmptyNotes(modifier: Modifier) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(38.dp),
                )
            }
            Text(
                text = "还没有笔记",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "在阅读器里点“摘句”，把值得记住的句子收进来。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SwipeDeleteContainer(
    enabled: Boolean = true,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val deleteWidth = 112.dp
    val deleteWidthPx = with(density) { deleteWidth.toPx() }
    var offsetX by remember { mutableStateOf(0f) }
    val deleteArmed = offsetX <= -deleteWidthPx * 0.9f
    LaunchedEffect(deleteArmed) {
        if (deleteArmed) {
            delay(5_000)
            offsetX = 0f
        }
    }
    LaunchedEffect(enabled) {
        if (!enabled) offsetX = 0f
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.errorContainer),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(deleteWidth)
                    .fillMaxHeight()
                    .clickable(enabled = enabled && deleteArmed) {
                        offsetX = 0f
                        onDelete()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = "再左拉删除",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        maxLines = 1,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(enabled, deleteWidthPx) {
                    if (!enabled) return@pointerInput
                    var shouldDelete = false
                    var wasArmedAtDragStart = false
                    detectHorizontalDragGestures(
                        onDragStart = {
                            shouldDelete = false
                            wasArmedAtDragStart = offsetX <= -deleteWidthPx * 0.9f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            val nextOffset = (offsetX + dragAmount)
                                .coerceIn(-deleteWidthPx * 1.28f, 0f)
                            if (wasArmedAtDragStart && nextOffset <= -deleteWidthPx * 1.12f) {
                                shouldDelete = true
                            }
                            offsetX = nextOffset
                            change.consume()
                        },
                        onDragEnd = {
                            if (shouldDelete) {
                                offsetX = 0f
                                onDelete()
                            } else if (wasArmedAtDragStart) {
                                offsetX = if (offsetX > -deleteWidthPx * 0.45f) {
                                    0f
                                } else {
                                    -deleteWidthPx
                                }
                            } else {
                                offsetX = if (offsetX < -1f) {
                                    -deleteWidthPx
                                } else {
                                    0f
                                }
                            }
                        },
                        onDragCancel = {
                            offsetX = if (wasArmedAtDragStart || offsetX < -1f) -deleteWidthPx else 0f
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun NoteCard(
    note: ReaderNote,
    noteFont: ReaderFont,
    displayMode: NotesDisplayMode,
    expanded: Boolean,
    batchDeleteMode: Boolean,
    selected: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onOpenSource: () -> Unit,
    onDelete: () -> Unit,
) {
    val showDetails = expanded || displayMode == NotesDisplayMode.DETAIL
    val showEnglishOnly = displayMode == NotesDisplayMode.ENGLISH && !showDetails
    val isChatNote = note.noteType == ReaderNoteType.CHAT
    SwipeDeleteContainer(enabled = !batchDeleteMode, onDelete = onDelete) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        if (batchDeleteMode) {
                            onSelectedChange(!selected)
                        } else if (displayMode != NotesDisplayMode.DETAIL) {
                            onToggleExpanded()
                        }
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (batchDeleteMode) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = onSelectedChange,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isChatNote) Icons.AutoMirrored.Filled.Chat else Icons.Filled.BookmarkAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = note.noteType.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        if (!batchDeleteMode) {
                            IconButton(onClick = onOpenSource) {
                                Icon(Icons.Filled.AutoStories, contentDescription = "回原文")
                            }
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Filled.Edit, contentDescription = "编辑")
                            }
                        }
                    }
                    Text(
                        text = "${note.bookTitle} · 第 ${note.paragraphIndex + 1} 段 · ${formatTimestamp(note.updatedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isChatNote) {
                        NotePlainSection(
                            title = "原文",
                            text = if (showDetails) note.sentence else note.sentence.compactText(96),
                            emphasized = true,
                            maxLines = if (showDetails) Int.MAX_VALUE else 3,
                        )
                        if (!showEnglishOnly && note.noteText.isNotBlank()) {
                            if (showDetails) {
                                NoteMarkdownSection(title = "提问", markdown = note.noteText)
                            } else {
                                NotePlainSection(
                                    title = "提问",
                                    text = note.noteText.compactText(88),
                                    maxLines = 2,
                                )
                            }
                        }
                        if (!showEnglishOnly && note.translationText.isNotBlank()) {
                            if (showDetails) {
                                NoteMarkdownSection(title = "回答", markdown = note.translationText)
                            } else {
                                NotePlainSection(
                                    title = "回答",
                                    text = note.translationText.compactText(120),
                                    maxLines = 3,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (showDetails) note.sentence else note.sentence.compactText(96),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = if (showDetails) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!showEnglishOnly && note.translationText.isNotBlank()) {
                            Text(
                                text = if (showDetails) note.translationText else note.translationText.compactText(96),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (showDetails) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (showDetails && note.noteText.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "自己的笔记",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = note.noteText,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = noteFont.toFontFamily()),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (!showDetails && !showEnglishOnly && note.noteText.isNotBlank()) {
                            Text(
                                text = note.noteText.compactText(56),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = noteFont.toFontFamily()),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotePlainSection(
    title: String,
    text: String,
    emphasized: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = text,
            style = if (emphasized) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = if (emphasized) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (emphasized) FontWeight.Medium else null,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun NoteMarkdownSection(
    title: String,
    markdown: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        MarkdownText(
            markdown = markdown,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LookupHistoryCard(
    item: LookupHistoryEntry,
    displayMode: NotesDisplayMode,
    expanded: Boolean,
    batchDeleteMode: Boolean,
    selected: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectedChange: (Boolean) -> Unit,
    onOpenSource: () -> Unit,
    onDelete: () -> Unit,
) {
    val showDetails = expanded || displayMode == NotesDisplayMode.DETAIL
    val showEnglishOnly = displayMode == NotesDisplayMode.ENGLISH && !showDetails
    SwipeDeleteContainer(enabled = !batchDeleteMode, onDelete = onDelete) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        if (batchDeleteMode) {
                            onSelectedChange(!selected)
                        } else if (displayMode != NotesDisplayMode.DETAIL) {
                            onToggleExpanded()
                        }
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (batchDeleteMode) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = onSelectedChange,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (item.type == LookupHistoryType.WORD) Icons.Filled.History else Icons.Filled.Translate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = item.type.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        if (!batchDeleteMode) {
                            IconButton(onClick = onOpenSource) {
                                Icon(Icons.Filled.AutoStories, contentDescription = "回原文")
                            }
                        }
                    }
                    Text(
                        text = "${item.bookTitle} · 第 ${item.paragraphIndex + 1} 段 · ${formatTimestamp(item.updatedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = if (showDetails) item.sourceText else item.sourceText.compactText(96),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = if (showDetails) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (showDetails && item.phonetic.isNotBlank()) {
                        Text(
                            text = item.phonetic,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (!showEnglishOnly) {
                        val resultText = when {
                            showDetails -> item.resultText
                            item.type == LookupHistoryType.WORD -> item.summaryMeaning()
                            else -> item.resultText.compactText(96)
                        }
                        if (resultText.isNotBlank()) {
                            Text(
                                text = resultText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (showDetails) Int.MAX_VALUE else 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditNoteDialog(
    note: ReaderNote,
    noteFont: ReaderFont,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var text by remember(note.id) { mutableStateOf(note.noteText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
        title = { Text("编辑笔记") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = note.sentence,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (note.translationText.isNotBlank()) {
                    Text(
                        text = note.translationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp),
                    )
                }
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    minLines = 4,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = noteFont.toFontFamily()),
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("自己的笔记") },
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(text) }, shape = RoundedCornerShape(8.dp)) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
