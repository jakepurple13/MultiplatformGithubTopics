// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
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

    application {
        val vm = remember { AppViewModel() }
        val scope = rememberCoroutineScope()
        var showThemeSelector by remember { mutableStateOf(false) }
        val themeColors by currentThemes.collectAsState(ThemeColors.Default)
        val isDarkMode by isDarkModes.collectAsState(true)
        val snackbarHostState = remember { SnackbarHostState() }
        var showLibrariesUsed by remember { mutableStateOf(false) }

        Theme(
            themeColors = themeColors,
            isDarkMode = isDarkMode,
            appActions = AppActions(
                onCardClick = vm::newTab,
                onNewTabOpen = vm::newTabAndOpen,
                onShareClick = {
                    val stringSelection = StringSelection(it.htmlUrl)
                    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(stringSelection, null)
                    Toolkit.getDefaultToolkit().beep()
                    scope.launch { snackbarHostState.showSnackbar("Copied") }
                },
                onSettingsClick = { showThemeSelector = true },
                showLibrariesUsed = { showLibrariesUsed = true }
            )
        ) {
            val topicViewModel = remember { TopicViewModel(scope, s) }
            WindowWithBar(
                windowTitle = "GitHub Topics",
                onCloseRequest = ::exitApplication,
                snackbarHostState = snackbarHostState,
                frameWindowScope = {
                    MenuOptions(
                        isDarkMode = isDarkMode,
                        onModeChange = { scope.launch { db.changeMode(it) } },
                        onShowColors = { showThemeSelector = true },
                        refresh = { scope.launch { topicViewModel.refresh() } },
                        previousTab = {
                            if (vm.selected == 0) {
                                vm.selected = vm.repoTabs.size
                            } else {
                                vm.selected--
                            }
                        },
                        nextTab = {
                            if (vm.selected == vm.repoTabs.size) {
                                vm.selected = 0
                            } else {
                                vm.selected++
                            }
                        },
                        closeTabEnabled = vm.selected != 0,
                        closeTab = { vm.closeTab(vm.repoTabs[vm.selected - 1]) }
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
                                    selected = true,
                                    text = { Text("Topics") },
                                    onClick = { vm.selected = 0 },
                                    icon = { Icon(Icons.Default.Topic, null) }
                                )

                                vm.repoTabs.forEachIndexed { index, topic ->
                                    LeadingIconTab(
                                        selected = true,
                                        text = { Text(topic.name) },
                                        onClick = { vm.selected = index + 1 },
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
                            0 -> App(topicViewModel)
                            else -> {
                                key(vm.selected, vm.refreshKey) {
                                    vm.repoTabs.getOrNull(vm.selected - 1)?.let { topic ->
                                        GithubRepo(
                                            vm = remember { RepoViewModel(Json.encodeToString(topic)) },
                                            backAction = { vm.closeTab(topic) }
                                        )
                                    }
                                }
                            }
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
                )
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
    onShowColors: () -> Unit,
    refresh: (() -> Unit)? = null,
    previousTab: () -> Unit = {},
    nextTab: () -> Unit = {},
    closeTabEnabled: Boolean = false,
    closeTab: () -> Unit = {}
) {
    MenuBar {
        Menu("Theme", mnemonic = 'T') {
            CheckboxItem(
                "Light/Dark Mode",
                checked = isDarkMode,
                onCheckedChange = onModeChange
            )

            Item(
                "Colors",
                onClick = onShowColors
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
        Menu("Navigation") {
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
        }
    }
}

class AppViewModel {
    val repoTabs = mutableStateListOf<GitHubTopic>()
    var selected by mutableStateOf(0)
    var refreshKey by mutableStateOf(0)

    fun newTab(topic: GitHubTopic) {
        if (topic !in repoTabs)
            repoTabs.add(topic)
    }

    fun newTabAndOpen(topic: GitHubTopic) {
        repoTabs.add(topic)
        selected = repoTabs.indexOf(topic) + 1
    }

    fun closeTab(topic: GitHubTopic) {
        val index = repoTabs.indexOf(topic) + 1
        when {
            index < selected -> selected--
            index == selected -> {
                if (repoTabs.size > selected) {
                    refreshKey++
                } else {
                    selected--
                }
            }

            index > selected -> Unit
            else -> selected = 0
        }
        repoTabs.remove(topic)
    }
}
