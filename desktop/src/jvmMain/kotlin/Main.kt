// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.application
import com.example.common.*
import io.realm.kotlin.ext.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
fun mains() {
    val db = Database()
    val info = runBlocking {
        val f = db.realm.query(SettingInformation::class).first().find()
        f ?: db.realm.write { copyToRealm(SettingInformation()) }
    }

    val s = info.asFlow().mapNotNull { it.obj }

    val isDarkModes = s
        .map { it.isDarkMode }
        .distinctUntilChanged()

    val currentThemes = s
        .map { it.theme }
        .map { ThemeColors.values()[it] }
        .distinctUntilChanged()

    val closeOnExit = s
        .map { it.closeOnExit }
        .distinctUntilChanged()

    application {
        val vm = remember { AppViewModel() }
        val scope = rememberCoroutineScope()
        val favoritesVM = remember { FavoritesViewModel(scope, db) }
        val topicViewModel = remember { TopicViewModel(scope, s) }
        var showThemeSelector by remember { mutableStateOf(false) }
        val themeColors by currentThemes.collectAsState(ThemeColors.Default)
        val isDarkMode by isDarkModes.collectAsState(true)
        val canCloseOnExit by closeOnExit.collectAsState(false)
        val snackbarHostState = remember { SnackbarHostState() }
        var showLibrariesUsed by remember { mutableStateOf(false) }
        val browserHandler = LocalBrowserHandler.current

        Theme(
            themeColors = themeColors,
            isDarkMode = isDarkMode,
            appActions = AppActions(
                onCardClick = vm::newTab,
                onNewTabOpen = vm::newTabAndOpen,
                onNewWindow = vm.repoWindows::add,
                onShareClick = {
                    val stringSelection = StringSelection(it.htmlUrl)
                    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(stringSelection, null)
                    Toolkit.getDefaultToolkit().beep()
                    scope.launch { snackbarHostState.showSnackbar("Copied") }
                },
                onSettingsClick = { showThemeSelector = true },
                showLibrariesUsed = { showLibrariesUsed = true },
                showFavorites = { vm.selectTab(0) }
            )
        ) {
            Tray(
                icon = painterResource("logo.png"),
                onAction = { vm.showTopicWindow = true },
                menu = {
                    if (!canCloseOnExit) Item("Open Window", onClick = { vm.showTopicWindow = true })
                    Item("Quit App", onClick = ::exitApplication)
                }
            )

            WindowWithBar(
                visible = vm.showTopicWindow,
                windowTitle = "GitHub Topics",
                onCloseRequest = if (canCloseOnExit) ::exitApplication
                else {
                    { vm.showTopicWindow = false }
                },
                onPreviewKeyEvent = {
                    if (it.type == KeyEventType.KeyDown) {
                        when {
                            it.isMetaPressed && it.key == Key.W && (vm.selected != 0 && vm.selected != 1) -> {
                                vm.closeSelectedTab()
                                true
                            }

                            it.isCtrlPressed && it.isShiftPressed && it.key == Key.Tab -> {
                                vm.previousTab()
                                true
                            }

                            it.isCtrlPressed && it.key == Key.Tab -> {
                                vm.nextTab()
                                true
                            }

                            it.isMetaPressed && it.isShiftPressed && it.key == Key.T -> {
                                vm.reopenTabOrWindow()
                                true
                            }

                            else -> false
                        }
                    } else false
                },
                snackbarHostState = snackbarHostState,
                frameWindowScope = {
                    MenuOptions(
                        isDarkMode = isDarkMode,
                        onModeChange = { scope.launch { db.changeMode(it) } },
                        onShowSettings = { showThemeSelector = true },
                        refresh = { scope.launch { topicViewModel.refresh() } },
                        previousTab = vm::previousTab,
                        nextTab = vm::nextTab,
                        closeTabEnabled = vm.selected != 0 && vm.selected != 1,
                        closeTab = vm::closeSelectedTab,
                        canReopen = vm.canReopen,
                        reopen = vm::reopenTabOrWindow,
                        hasHistory = vm.canReopen,
                        openHistory = vm::openHistory
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        Column {
                            ScrollableTabRow(
                                selectedTabIndex = vm.selected.coerceIn(0, vm.browserTab.tabbed.size - 1),
                                edgePadding = 0.dp
                            ) {
                                vm.browserTab.tabsList.forEach { (index, tab) ->
                                    when (tab) {
                                        is Tabs.PinnedTab<TabType> -> {
                                            when (index) {
                                                0 -> {
                                                    LeadingIconTab(
                                                        selected = vm.selected == 0,
                                                        text = { Text("Favorites") },
                                                        onClick = { vm.selectTab(0) },
                                                        icon = { Icon(Icons.Default.Favorite, null) }
                                                    )
                                                }

                                                1 -> {
                                                    LeadingIconTab(
                                                        selected = vm.selected == 1,
                                                        text = { Text("Topics") },
                                                        onClick = { vm.selectTab(1) },
                                                        icon = { Icon(Icons.Default.Topic, null) }
                                                    )
                                                }
                                            }
                                        }

                                        is Tabs.Tab<TabType> -> {
                                            val topic = (tab.data as TabType.Normal).topic
                                            LeadingIconTab(
                                                selected = vm.selected == index,
                                                text = { Text(topic.name) },
                                                onClick = { vm.selectTab(index) },
                                                modifier = Modifier.onPointerEvent(PointerEventType.Press) {
                                                    val isMiddleClick = it.button == PointerButton.Tertiary
                                                    if (isMiddleClick) vm.closeTab(tab)
                                                },
                                                icon = {
                                                    IconsButton(
                                                        onClick = { vm.closeTab(tab) },
                                                        icon = Icons.Default.Close
                                                    )
                                                }
                                            )
                                        }

                                        is Tabs.EndTab<TabType> -> {
                                            AnimatedVisibility(vm.showHistory) {
                                                LeadingIconTab(
                                                    selected = vm.selected == vm.browserTab.tabbed.lastIndex,
                                                    text = { Text("History") },
                                                    onClick = {},
                                                    icon = { Icon(Icons.Default.History, null) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                ) { p ->
                    Box(modifier = Modifier.padding(p)) {
                        when (val tab = vm.browserTab.selectedTab()) {
                            is Tabs.PinnedTab<TabType> -> {
                                when ((tab.data as TabType.Pinned).index) {
                                    0 -> FavoritesUi(favoritesVM) { vm.selectTab(0) }
                                    1 -> App(topicViewModel, favoritesVM)
                                }
                            }

                            is Tabs.Tab<TabType> -> {
                                key(vm.selected, vm.browserTab.refreshKey) {
                                    (tab.data as? TabType.Normal)?.topic?.let { topic ->
                                        val repo = remember {
                                            RepoViewModel(Json.encodeToString(topic), browserHandler)
                                        }
                                        GithubRepo(
                                            vm = repo,
                                            favoritesVM = favoritesVM,
                                            backAction = { vm.closeTab(tab) }
                                        )
                                        DisposableEffect(Unit) {
                                            onDispose { repo.browser.close(true) }
                                        }
                                    }
                                }
                            }

                            is Tabs.EndTab<TabType> -> {
                                HistoryUi(
                                    vm = vm,
                                    favoritesVM = favoritesVM,
                                    backAction = { vm.selectTab(1) }
                                )
                            }

                            else -> Unit
                        }
                    }
                }
            }

            vm.repoWindows.forEach { topic ->
                val topicSnackbarHostState = remember { SnackbarHostState() }

                CompositionLocalProvider(
                    LocalAppActions provides LocalAppActions.current.copy(
                        onShareClick = {
                            val stringSelection = StringSelection(it.htmlUrl)
                            val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            clipboard.setContents(stringSelection, null)
                            Toolkit.getDefaultToolkit().beep()
                            scope.launch { topicSnackbarHostState.showSnackbar("Copied") }
                        }
                    )
                ) {
                    WindowWithBar(
                        windowTitle = topic.name,
                        onCloseRequest = { vm.closeWindow(topic) },
                        snackbarHostState = topicSnackbarHostState,
                        frameWindowScope = {
                            MenuOptions(
                                isDarkMode = isDarkMode,
                                onModeChange = { scope.launch { db.changeMode(it) } },
                                onShowSettings = { showThemeSelector = true }
                            )
                        }
                    ) {
                        val repo = remember { RepoViewModel(Json.encodeToString(topic), browserHandler) }
                        GithubRepo(
                            vm = repo,
                            favoritesVM = favoritesVM,
                            backAction = { vm.closeWindow(topic) }
                        )

                        DisposableEffect(Unit) {
                            onDispose { repo.browser.close(true) }
                        }
                    }
                }
            }

            WindowWithBar(
                windowTitle = "Settings",
                onCloseRequest = { showThemeSelector = false },
                visible = showThemeSelector
            ) {
                SettingsScreen(
                    currentThemeColors = themeColors,
                    setCurrentThemeColors = { scope.launch { db.changeTheme(it) } },
                    isDarkMode = isDarkMode,
                    onModeChange = { scope.launch { db.changeMode(it) } }
                ) {
                    NavigationDrawerItem(
                        label = { Text("Close Application on Exit? Or just hide window?") },
                        badge = {
                            Switch(
                                checked = canCloseOnExit,
                                onCheckedChange = { scope.launch { db.changeCloseOnExit(it) } }
                            )
                        },
                        onClick = { scope.launch { db.changeCloseOnExit(!canCloseOnExit) } },
                        selected = true,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Divider()
                }
            }

            WindowWithBar(
                onCloseRequest = { showLibrariesUsed = false },
                windowTitle = "Libraries Used",
                visible = showLibrariesUsed
            ) { LibrariesUsed { showLibrariesUsed = false } }

        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FrameWindowScope.MenuOptions(
    isDarkMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onShowSettings: () -> Unit,
    refresh: (() -> Unit)? = null,
    previousTab: () -> Unit = {},
    nextTab: () -> Unit = {},
    closeTabEnabled: Boolean = false,
    closeTab: () -> Unit = {},
    canReopen: Boolean = false,
    reopen: () -> Unit = {},
    hasHistory: Boolean = false,
    openHistory: () -> Unit = {}
) {
    MenuBar {
        Menu("Settings", mnemonic = 'T') {
            CheckboxItem(
                "Light/Dark Mode",
                checked = isDarkMode,
                onCheckedChange = onModeChange
            )

            Item(
                "Settings",
                onClick = onShowSettings
            )

            Item(
                "History",
                enabled = hasHistory,
                onClick = openHistory
            )
        }

        refresh?.let { r ->
            Menu("Options") {
                Item(
                    "Refresh",
                    onClick = r,
                    shortcut = KeyShortcut(
                        meta = hostOs == OS.MacOS,
                        ctrl = hostOs == OS.Windows || hostOs == OS.Linux,
                        key = Key.R
                    )
                )

                Item("Libraries Used", onClick = LocalAppActions.current.showLibrariesUsed)
            }
        }
        Menu("Window") {
            Item(
                "Previous Tab",
                onClick = previousTab,
                shortcut = KeyShortcut(
                    ctrl = true,
                    shift = true,
                    key = Key.Tab
                )
            )
            Item(
                "Next Tab",
                onClick = nextTab,
                shortcut = KeyShortcut(
                    ctrl = true,
                    key = Key.Tab
                )
            )
            Item(
                "Close Tab",
                enabled = closeTabEnabled,
                onClick = closeTab,
                shortcut = KeyShortcut(
                    meta = hostOs == OS.MacOS,
                    ctrl = hostOs == OS.Windows || hostOs == OS.Linux,
                    key = Key.W
                )
            )

            Item(
                "Reopen Tab/Window",
                enabled = canReopen,
                onClick = reopen,
                shortcut = KeyShortcut(
                    meta = hostOs == OS.MacOS,
                    ctrl = hostOs == OS.Windows || hostOs == OS.Linux,
                    shift = true,
                    key = Key.T
                )
            )
        }
    }
}

sealed class TabType {
    class Pinned(val index: Int) : TabType()
    class Normal(val topic: GitHubTopic) : TabType()
}

class AppViewModel {
    val browserTab = BrowserTab<TabType>(1).apply {
        newPinnedTab(TabType.Pinned(0))
        newPinnedTab(TabType.Pinned(1))
        hasEndTab(true)
    }
    var showTopicWindow by mutableStateOf(true)
    var showHistory by mutableStateOf(false)
    val repoWindows = mutableStateListOf<GitHubTopic>()
    private val closedTabRepos = mutableStateListOf<GitHubTopic>()
    private val closedWindowRepos = mutableStateListOf<GitHubTopic>()
    private var closedWindowType = WindowType.None
    var selected
        get() = browserTab.selected
        set(value) = browserTab.selectTab(value)

    val closedItems by derivedStateOf {
        mapOf(
            WindowType.Tab to closedTabRepos,
            WindowType.Window to closedWindowRepos
        )
    }

    enum class WindowType { Tab, Window, None }

    val canReopen by derivedStateOf { closedTabRepos.isNotEmpty() || closedWindowRepos.isNotEmpty() }

    fun reopenTabOrWindow() {
        when (closedWindowType) {
            WindowType.Tab -> {
                closedTabRepos.lastOrNull()
                    ?.also(closedTabRepos::remove)
                    ?.let(::newTab)
            }

            WindowType.Window -> {
                closedWindowRepos.lastOrNull()
                    ?.also(closedWindowRepos::remove)
                    ?.let(repoWindows::add)
            }

            WindowType.None -> Unit
        }
        when {
            closedWindowRepos.isEmpty() && closedTabRepos.isEmpty() -> closedWindowType = WindowType.None
            closedTabRepos.isEmpty() -> closedWindowType = WindowType.Window
            closedWindowRepos.isEmpty() -> closedWindowType = WindowType.Tab
        }
    }

    fun reopenTab(topic: GitHubTopic) {
        closedTabRepos.remove(topic)
        newTab(topic)

        when {
            closedWindowRepos.isEmpty() && closedTabRepos.isEmpty() -> closedWindowType = WindowType.None
            closedTabRepos.isEmpty() -> closedWindowType = WindowType.Window
            closedWindowRepos.isEmpty() -> closedWindowType = WindowType.Tab
        }
    }

    fun reopenWindow(topic: GitHubTopic) {
        closedWindowRepos.remove(topic)
        repoWindows.add(topic)

        when {
            closedWindowRepos.isEmpty() && closedTabRepos.isEmpty() -> closedWindowType = WindowType.None
            closedTabRepos.isEmpty() -> closedWindowType = WindowType.Window
            closedWindowRepos.isEmpty() -> closedWindowType = WindowType.Tab
        }
    }

    fun closeWindow(topic: GitHubTopic) {
        repoWindows.remove(topic)
        if (topic !in closedTabRepos && topic !in closedWindowRepos) {
            closedWindowType = WindowType.Window
            closedWindowRepos.add(topic)
        }
    }

    fun newTab(topic: GitHubTopic) {
        if (
            browserTab.tabbed.filterIsInstance<Tabs.Tab<TabType.Normal>>()
                .none { it.data.topic.htmlUrl == topic.htmlUrl }
        ) browserTab.newTab(TabType.Normal(topic))
    }

    fun newTabAndOpen(topic: GitHubTopic) {
        newTab(topic)
    }

    fun closeTab(topic: Tabs.Tab<TabType>) {
        browserTab.closeTab(topic)
        val topicData = (topic.data as TabType.Normal).topic
        if (topicData !in closedTabRepos && topicData !in closedWindowRepos) {
            closedWindowType = WindowType.Tab
            closedTabRepos.add(topicData)
        }
    }

    fun nextTab() {
        showHistory = false
        browserTab.nextTab()
    }

    fun previousTab() {
        showHistory = false
        browserTab.previousTab()
    }

    fun selectTab(index: Int) {
        showHistory = false
        browserTab.selectTab(index)
    }

    fun closeSelectedTab() {
        val b = browserTab.selectedTab()
        if (b is Tabs.Tab<TabType>) browserTab.closeTab(b)
    }

    fun openHistory() {
        showHistory = true
        browserTab.selectTab(browserTab.tabbed.lastIndex)
    }
}
