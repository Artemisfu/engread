package com.engread.app

import android.app.Activity
import android.view.KeyEvent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.media.AudioAttributes
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChangedIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.engread.app.data.Book
import com.engread.app.data.LibraryRepository
import com.engread.app.data.LookupHistoryEntry
import com.engread.app.data.LookupHistoryType
import com.engread.app.data.ReaderFont
import com.engread.app.data.ReaderNote
import com.engread.app.data.ReaderSettings
import com.engread.app.data.ReaderTheme
import com.engread.app.reader.BookChapter
import com.engread.app.reader.EcdictDictionary
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.math.roundToInt

private object ReaderVolumeKeyPager {
    var previous: (() -> Unit)? = null
    var next: (() -> Unit)? = null

    fun clear(previousHandler: () -> Unit, nextHandler: () -> Unit) {
        if (previous === previousHandler) previous = null
        if (next === nextHandler) next = null
    }
}

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
    object Settings : AppScreen()
    data class Reader(val bookId: String) : AppScreen()
}

private val AppScreenSaver = Saver<AppScreen, String>(
    save = { screen ->
        when (screen) {
            AppScreen.Shelf -> "shelf"
            AppScreen.Notes -> "notes"
            AppScreen.Settings -> "settings"
            is AppScreen.Reader -> "reader:${screen.bookId}"
        }
    },
    restore = { value ->
        when {
            value == "notes" -> AppScreen.Notes
            value == "settings" -> AppScreen.Settings
            value.startsWith("reader:") && value.length > "reader:".length -> {
                AppScreen.Reader(value.substringAfter("reader:"))
            }
            else -> AppScreen.Shelf
        }
    },
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
    var settings by remember { mutableStateOf(repository.getSettings()) }
    var lastHomeBackAt by remember { mutableStateOf(0L) }

    fun refreshAll() {
        books = repository.getBooks()
        notes = repository.getNotes()
        lookupHistory = repository.getLookupHistory()
        settings = repository.getSettings()
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
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

    EngReadTheme(readerTheme = settings.theme) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val current = screen) {
                AppScreen.Shelf -> ShelfScreen(
                    books = books,
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
                    onOpenBook = { screen = AppScreen.Reader(it.id) },
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
                            showMessage("笔记已删除")
                        }
                    },
                    onDeleteNotes = { selectedNotes ->
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                selectedNotes.forEach { repository.deleteNote(it.id) }
                            }
                            refreshAll()
                            showMessage("已删除 ${selectedNotes.size} 条笔记")
                        }
                    },
                    onClearHistory = {
                        scope.launch {
                            withContext(Dispatchers.IO) { repository.clearLookupHistory() }
                            refreshAll()
                            showMessage("查词历史已清空")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfScreen(
    books: List<Book>,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit,
    onImport: () -> Unit,
    onOpenBook: (Book) -> Unit,
    onDeleteBook: (Book) -> Unit,
) {
    var pendingDelete by remember { mutableStateOf<Book?>(null) }
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
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("删除书籍？") },
            text = { Text("《${book.title}》和它的笔记会从本机移除。") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDelete = null
                        onDeleteBook(book)
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
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
    var wordStack by remember(book.id) { mutableStateOf<List<WordEntry>>(emptyList()) }
    var wordLookupSerial by remember(book.id) { mutableStateOf(0) }
    var translationText by remember(book.id) { mutableStateOf<String?>(null) }
    var translationLoading by remember(book.id) { mutableStateOf(false) }
    var translationIsError by remember(book.id) { mutableStateOf(false) }
    var selectionStart by remember(page.index) { mutableStateOf<Int?>(null) }
    var selectionEnd by remember(page.index) { mutableStateOf<Int?>(null) }
    var wordSelectionRange by remember(page.index) { mutableStateOf<IntRange?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    var ttsStatusText by remember { mutableStateOf("TTS 正在初始化") }
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

        fun finishTtsInit(status: Int) {
            val activeEngine = engine
            if (activeEngine == null) {
                mainHandler.postDelayed({ finishTtsInit(status) }, 50)
                return
            }
            if (status == TextToSpeech.SUCCESS) {
                activeEngine.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                val languageStatus = activeEngine.setLanguage(Locale.US)
                val ready = languageStatus != TextToSpeech.LANG_MISSING_DATA &&
                    languageStatus != TextToSpeech.LANG_NOT_SUPPORTED
                mainHandler.post {
                    ttsReady = ready
                    ttsStatusText = if (ready) {
                        "TTS 已就绪"
                    } else {
                        "手机 TTS 缺少或不支持英文语音"
                    }
                }
            } else {
                ttsReady = false
                ttsStatusText = "手机 TTS 初始化失败"
            }
        }

        engine = TextToSpeech(context.applicationContext) { status ->
            mainHandler.post { finishTtsInit(status) }
        }
        engine.setOnUtteranceProgressListener(
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
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
            ttsReady = false
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
            wordStack = if (replaceStack) listOf(dictionaryEntry) else wordStack + dictionaryEntry
            translationText = null
            onAddLookupHistory(
                paragraphIndex,
                LookupHistoryType.WORD,
                dictionaryEntry.word,
                dictionaryEntry.toLookupHistoryText(),
                dictionaryEntry.phonetic,
            )
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
                    entry.phonetic,
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
                    onClose = { closeTopWordCard() },
                    onLookupWord = { lookupWord(it) },
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
                    text = text,
                    loading = translationLoading,
                    isError = translationIsError,
                    onAddNote = if (selectedText.isNotBlank()) {
                        {
                            openNoteDialog(
                                sourceText = selectedText,
                                knownTranslation = text.takeUnless { translationLoading || translationIsError },
                            )
                        }
                    } else {
                        null
                    },
                    onClose = {
                        translationText = null
                        translationLoading = false
                        translationIsError = false
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
                    clearWordLookup()
                },
                selectedText = selectedText,
                onTranslateSelection = {
                    if (selectedText.isNotBlank()) {
                        translationText = "查阅中..."
                        translationLoading = true
                        translationIsError = false
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
                onNoteSelection = {
                    if (selectedText.isNotBlank()) {
                        openNoteDialog(selectedText)
                    }
                },
                onClearSelection = {
                    selectionStart = null
                    selectionEnd = null
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
                selectionStart = null
                selectionEnd = null
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
    modifier: Modifier = Modifier,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onBack: () -> Unit,
    onOpenToc: () -> Unit,
    onOpenPageSettings: () -> Unit,
    onWordLongPress: (String, Int, IntRange?) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onTranslateSelection: () -> Unit,
    onNoteSelection: () -> Unit,
    onClearSelection: () -> Unit,
) {
    var layoutResult by remember(page.text) { mutableStateOf<TextLayoutResult?>(null) }
    var textOrigin by remember(page.text) { mutableStateOf(Offset.Zero) }
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

    LaunchedEffect(controlsVisible, controlsVisibleEpoch) {
        if (controlsVisible) {
            delay(3_000)
            controlsVisible = false
        }
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(page.index, pageCount, titleTapHeight) {
                detectReaderPageTapGestures(
                    titleTapHeight = titleTapHeight,
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
                    .pointerInput(bookTitle, chapterTitle, page.index) {
                        detectTapGestures(onTap = { showControls() })
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
                    .onGloballyPositioned { coordinates ->
                        textOrigin = coordinates.positionInParent()
                    }
                    .pointerInput(page.text) {
                        detectReaderTextGestures(
                            text = page.text,
                            getLayoutResult = { layoutResult },
                            canLookupWord = { selectedText.isBlank() },
                            onWordLongPress = onWordLongPress,
                            paragraphIndexForOffset = { page.paragraphIndexForDisplayOffset(it) },
                            onSelectionGestureStart = {},
                            onSelectionChange = onSelectionChange,
                            onSelectionGestureEnd = {},
                        )
                    },
                onTextLayout = { layoutResult = it },
            )

            Spacer(Modifier.weight(1f))

            if (selectedText.isNotBlank()) {
                SelectionActionBar(
                    onTranslate = onTranslateSelection,
                    onAddNote = onNoteSelection,
                    onClear = onClearSelection,
                )
            }
        }

        SelectionHandles(
            text = page.text,
            textOrigin = textOrigin,
            layoutResult = layoutResult,
            selectionRange = selectionRange,
            onSelectionChange = onSelectionChange,
        )

        if (controlsVisible) {
            ReaderTopControls(
                title = bookTitle,
                onBack = onBack,
                onOpenToc = onOpenToc,
                onOpenPageSettings = onOpenPageSettings,
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
    textOrigin: Offset,
    layoutResult: TextLayoutResult?,
    selectionRange: IntRange?,
    onSelectionChange: (Int, Int) -> Unit,
) {
    val layout = layoutResult ?: return
    val range = selectionRange ?: return
    if (text.isBlank()) return

    val startOffset = range.first.coerceIn(0, text.lastIndex)
    val endExclusive = (range.last + 1).coerceIn(0, text.length)
    val startRect = layout.getCursorRect(startOffset)
    val endRect = layout.getCursorRect(endExclusive)
    val startCursor = Offset(startRect.left, startRect.top)
    val endCursor = Offset(endRect.left, endRect.top)

    SelectionHandle(
        parentPosition = textOrigin + Offset(startRect.left, startRect.bottom),
        cursorPositionInText = startCursor,
        layoutResult = layout,
        fixedOffset = range.last,
        isStart = true,
        onSelectionChange = onSelectionChange,
    )
    SelectionHandle(
        parentPosition = textOrigin + Offset(endRect.left, endRect.bottom),
        cursorPositionInText = endCursor,
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
    layoutResult: TextLayoutResult,
    fixedOffset: Int,
    isStart: Boolean,
    onSelectionChange: (Int, Int) -> Unit,
) {
    val density = LocalDensity.current
    val horizontalInset = with(density) { 13.dp.toPx() }
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (parentPosition.x - horizontalInset).roundToInt(),
                    y = (parentPosition.y - 2f).roundToInt(),
                )
            }
            .size(width = 26.dp, height = 42.dp)
            .pointerInput(layoutResult, fixedOffset, isStart, cursorPositionInText) {
                var dragPosition = cursorPositionInText
                detectDragGestures(
                    onDragStart = { dragPosition = cursorPositionInText },
                    onDrag = { change, dragAmount ->
                        dragPosition += dragAmount
                        val offset = layoutResult.getOffsetForPosition(dragPosition)
                        if (isStart) {
                            onSelectionChange(offset, fixedOffset)
                        } else {
                            onSelectionChange(fixedOffset, offset)
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
                    .height(24.dp)
                    .background(primary),
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(primary),
            )
        }
    }
}

private suspend fun PointerInputScope.detectReaderPageTapGestures(
    titleTapHeight: Float,
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
            position.x < size.width * 0.28f -> onPreviousPage()
            position.x > size.width * 0.72f -> onNextPage()
        }
    }
}

private suspend fun PointerInputScope.detectReaderTextGestures(
    text: String,
    getLayoutResult: () -> TextLayoutResult?,
    canLookupWord: () -> Boolean,
    onWordLongPress: (String, Int, IntRange?) -> Unit,
    paragraphIndexForOffset: (Int) -> Int,
    onSelectionGestureStart: () -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onSelectionGestureEnd: () -> Unit,
) {
    awaitEachGesture {
        val firstDown = awaitFirstDown(requireUnconsumed = false)
        val upBeforeLongPress = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            waitForUpOrCancellation()
        }
        if (upBeforeLongPress != null) {
            return@awaitEachGesture
        }

        firstDown.consume()
        val layout = getLayoutResult() ?: return@awaitEachGesture
        val longPressOffset = layout.getOffsetForPosition(firstDown.position)
        val word = extractWordAt(text, longPressOffset)
        val wordRange = wordRangeAt(text, longPressOffset)
        val paragraphRange = paragraphRangeAt(text, longPressOffset)
        val pointerId = firstDown.id
        var selecting = false

        try {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId }
                    ?: event.changes.firstOrNull()
                    ?: continue
                if (change.changedToUpIgnoreConsumed() || !change.pressed) {
                    if (!selecting && word != null && canLookupWord()) {
                        change.consume()
                        onWordLongPress(word, paragraphIndexForOffset(longPressOffset), wordRange)
                    }
                    break
                }
                if (change.positionChangedIgnoreConsumed()) {
                    val currentLayout = getLayoutResult() ?: continue
                    if (!selecting) {
                        val movedEnough = (change.position - firstDown.position).getDistance() >
                            viewConfiguration.touchSlop
                        if (!movedEnough || paragraphRange == null) continue
                        selecting = true
                        onSelectionGestureStart()
                        onSelectionChange(paragraphRange.first, paragraphRange.last)
                    }
                    val currentOffset = currentLayout.getOffsetForPosition(change.position)
                    val activeParagraphRange = paragraphRange ?: continue
                    val anchorOffset = if (currentOffset < activeParagraphRange.first) {
                        activeParagraphRange.last
                    } else {
                        activeParagraphRange.first
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
private fun SelectionActionBar(
    onTranslate: () -> Unit,
    onAddNote: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalButton(onClick = onTranslate, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("翻译")
        }
        FilledTonalButton(onClick = onAddNote, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("摘句")
        }
        IconButton(onClick = onClear) {
            Icon(Icons.Filled.Close, contentDescription = "清除选择")
        }
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
    onClose: () -> Unit,
    onLookupWord: (String) -> Unit,
    onSpeak: () -> Unit,
) {
    val panelHeight = (LocalConfiguration.current.screenHeightDp * 0.38f).dp
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
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LookupText(
                        text = entry.word,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        onLookupWord = onLookupWord,
                    )
                    Text(
                        text = buildString {
                            append(entry.phonetic)
                            if (stackDepth > 1) append(" · $stackDepth 层")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                IconButton(onClick = onSpeak) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "播放读音")
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "关闭")
                }
            }
            HorizontalDivider()
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
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
                }
                if (entry.cognates.isNotEmpty()) {
                    WordListSection(
                        title = "同源词",
                        words = entry.cognates,
                        onLookupWord = onLookupWord,
                    )
                }
                if (entry.synonyms.isNotEmpty()) {
                    WordListSection(
                        title = "近义词",
                        words = entry.synonyms,
                        onLookupWord = onLookupWord,
                    )
                }
            }
        }
    }
}

@Composable
private fun WordInfoSection(
    title: String,
    text: String,
    onLookupWord: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
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
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
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
    onLookupWord: (String) -> Unit,
) {
    var layoutResult by remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        style = style,
        fontWeight = fontWeight,
        color = color,
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
    text: String,
    loading: Boolean,
    isError: Boolean,
    onAddNote: (() -> Unit)?,
    onClose: () -> Unit,
) {
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
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
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
    onDeleteNotes: (List<ReaderNote>) -> Unit,
    onClearHistory: () -> Unit,
) {
    var editingNote by remember { mutableStateOf<ReaderNote?>(null) }
    var deletingNote by remember { mutableStateOf<ReaderNote?>(null) }
    var batchDeleteConfirmOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var batchDeleteMode by remember { mutableStateOf(false) }
    var displayMode by remember { mutableStateOf(NotesDisplayMode.SUMMARY) }
    var filter by remember { mutableStateOf(NotesFilter.ALL) }
    val selectedNoteIds = remember { mutableStateMapOf<String, Boolean>() }
    val expandedCards = remember { mutableStateMapOf<String, Boolean>() }
    val selectedNotes = notes.filter { selectedNoteIds[it.id] == true }
    val activeFilter = if (batchDeleteMode) NotesFilter.NOTES else filter
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

    LaunchedEffect(notes) {
        selectedNoteIds.keys.toList().forEach { id ->
            if (notes.none { it.id == id }) selectedNoteIds.remove(id)
        }
        if (notes.isEmpty()) batchDeleteMode = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (batchDeleteMode) "已选 ${selectedNotes.size} 条" else "笔记",
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
                                    enabled = selectedNotes.isNotEmpty(),
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
                                        selectedNoteIds.clear()
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
                                    enabled = notes.isNotEmpty(),
                                    onClick = {
                                        menuOpen = false
                                        selectedNoteIds.clear()
                                        filter = NotesFilter.NOTES
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
                                    selected = selectedNoteIds[note.id] == true,
                                    onToggleExpanded = {
                                        expandedCards[timelineItem.id] = expandedCards[timelineItem.id] != true
                                    },
                                    onSelectedChange = { selected ->
                                        if (selected) {
                                            selectedNoteIds[note.id] = true
                                        } else {
                                            selectedNoteIds.remove(note.id)
                                        }
                                    },
                                    onEdit = { editingNote = note },
                                    onDelete = { deletingNote = note },
                                )
                            }

                            is NotesTimelineItem.LookupItem -> {
                                LookupHistoryCard(
                                    item = timelineItem.item,
                                    displayMode = displayMode,
                                    expanded = expandedCards[timelineItem.id] == true,
                                    onToggleExpanded = {
                                        expandedCards[timelineItem.id] = expandedCards[timelineItem.id] != true
                                    },
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
            title = { Text("删除所选笔记？") },
            text = { Text("将删除 ${selectedNotes.size} 条笔记，不会影响原书内容。") },
            confirmButton = {
                Button(
                    onClick = {
                        batchDeleteConfirmOpen = false
                        onDeleteNotes(selectedNotes)
                        selectedNoteIds.clear()
                        batchDeleteMode = false
                    },
                    enabled = selectedNotes.isNotEmpty(),
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
    onDelete: () -> Unit,
) {
    val showDetails = expanded || displayMode == NotesDisplayMode.DETAIL
    val showEnglishOnly = displayMode == NotesDisplayMode.ENGLISH && !showDetails
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
                        imageVector = Icons.Filled.BookmarkAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "摘句",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    if (!batchDeleteMode) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除")
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

@Composable
private fun LookupHistoryCard(
    item: LookupHistoryEntry,
    displayMode: NotesDisplayMode,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val showDetails = expanded || displayMode == NotesDisplayMode.DETAIL
    val showEnglishOnly = displayMode == NotesDisplayMode.ENGLISH && !showDetails
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .clickable(enabled = displayMode != NotesDisplayMode.DETAIL) { onToggleExpanded() }
                .padding(14.dp),
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
