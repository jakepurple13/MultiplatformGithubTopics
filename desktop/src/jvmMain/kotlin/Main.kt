// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
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
                showFavorites = { vm.selectTab(1) }
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
                                vm.repoTabs.getOrNull(vm.selected - 2)?.let { it1 -> vm.closeTab(it1) }
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
                        closeTab = { vm.closeTab(vm.repoTabs[vm.selected - 2]) },
                        canReopen = vm.canReopen,
                        reopen = vm::reopenTabOrWindow
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        Column {
                            ScrollableTabRow(
                                selectedTabIndex = vm.selected,
                                edgePadding = 0.dp
                            ) {
                                LeadingIconTab(
                                    selected = vm.selected == 0,
                                    text = { Text("Topics") },
                                    onClick = { vm.selectTab(0) },
                                    icon = { Icon(Icons.Default.Topic, null) }
                                )

                                LeadingIconTab(
                                    selected = vm.selected == 1,
                                    text = { Text("Favorites") },
                                    onClick = { vm.selectTab(1) },
                                    icon = { Icon(Icons.Default.Favorite, null) }
                                )

                                vm.repoTabs.forEachIndexed { index, topic ->
                                    LeadingIconTab(
                                        selected = vm.selected == index + 2,
                                        text = { Text(topic.name) },
                                        onClick = { vm.selectTab(index + 2) },
                                        modifier = Modifier.onPointerEvent(PointerEventType.Press) {
                                            val isMiddleClick = it.button == PointerButton.Tertiary
                                            if (isMiddleClick) vm.closeTab(topic)
                                        },
                                        icon = {
                                            IconsButton(
                                                onClick = { vm.closeTab(topic) },
                                                icon = Icons.Default.Close
                                            )
                                        }
                                    )
                                }
                            }
                            Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                ) { p ->
                    Box(modifier = Modifier.padding(p)) {
                        when (vm.selected) {
                            0 -> App(topicViewModel, favoritesVM)
                            1 -> FavoritesUi(favoritesVM) { vm.selectTab(0) }
                            else -> {
                                key(vm.selected, vm.refreshKey) {
                                    vm.repoTabs.getOrNull(vm.selected - 2)?.let { topic ->
                                        GithubRepo(
                                            vm = remember { RepoViewModel(Json.encodeToString(topic)) },
                                            favoritesVM = favoritesVM,
                                            backAction = { vm.closeTab(topic) }
                                        )
                                    }
                                }
                            }
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
                        GithubRepo(
                            vm = remember { RepoViewModel(Json.encodeToString(topic)) },
                            favoritesVM = favoritesVM,
                            backAction = { vm.closeWindow(topic) }
                        )
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
    reopen: () -> Unit = {}
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

class AppViewModel {
    var showTopicWindow by mutableStateOf(true)
    val repoTabs = mutableStateListOf<GitHubTopic>()
    val repoWindows = mutableStateListOf<GitHubTopic>()
    private val closedTabRepos = mutableStateListOf<GitHubTopic>()
    private val closedWindowRepos = mutableStateListOf<GitHubTopic>()
    private var closedWindowType = WindowType.None
    var selected by mutableStateOf(0)
    var refreshKey by mutableStateOf(0)

    private enum class WindowType { Tab, Window, None }

    val canReopen by derivedStateOf { closedTabRepos.isNotEmpty() || closedWindowRepos.isNotEmpty() }

    fun reopenTabOrWindow() {
        when (closedWindowType) {
            WindowType.Tab -> {
                closedTabRepos.lastOrNull()
                    ?.also(closedTabRepos::remove)
                    ?.let(repoTabs::add)
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

    fun closeWindow(topic: GitHubTopic) {
        repoWindows.remove(topic)
        if (topic !in closedTabRepos && topic !in closedWindowRepos) {
            closedWindowType = WindowType.Window
            closedWindowRepos.add(topic)
        }
    }

    fun newTab(topic: GitHubTopic) {
        if (topic !in repoTabs)
            repoTabs.add(topic)
    }

    fun newTabAndOpen(topic: GitHubTopic) {
        repoTabs.add(topic)
        selected = repoTabs.indexOf(topic) + 2
    }

    fun closeTab(topic: GitHubTopic) {
        val index = repoTabs.indexOf(topic) + 2
        when {
            index < selected -> selected--
            index == selected -> {
                if (repoTabs.size + 1 > selected) {
                    refreshKey++
                } else {
                    selected--
                }
            }

            index > selected -> Unit
            else -> selected = 0
        }
        repoTabs.remove(topic)
        if (topic !in closedTabRepos && topic !in closedWindowRepos) {
            closedWindowType = WindowType.Tab
            closedTabRepos.add(topic)
        }
    }

    fun nextTab() {
        if (selected == repoTabs.size + 1) {
            selected = 0
        } else {
            selected++
        }
    }

    fun previousTab() {
        if (selected == 0) {
            selected = repoTabs.size + 1
        } else {
            selected--
        }
    }

    fun selectTab(index: Int) {
        selected = index
    }
}
