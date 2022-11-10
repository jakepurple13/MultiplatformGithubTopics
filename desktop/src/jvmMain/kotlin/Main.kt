// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
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


fun mains() {
    val db = Database()
    val info = runBlocking {
        val f = db.realm.query<SettingInformation>(SettingInformation::class).first().find()
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

        val shareAction: (GitHubTopic) -> Unit = remember {
            {
                val stringSelection = StringSelection(it.htmlUrl)
                val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(stringSelection, null)
                Toolkit.getDefaultToolkit().beep()
            }
        }

        Theme(
            themeColors = themeColors,
            isDarkMode = isDarkMode,
            appActions = AppActions(
                onCardClick = vm::newWindow,
                onShareClick = {
                    shareAction(it)
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
                        refresh = { scope.launch { topicViewModel.refresh() } }
                    )
                }
            ) { App(topicViewModel) }

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

            vm.repoWindows.forEach { topic ->
                val topicSnackbarHostState = remember { SnackbarHostState() }

                CompositionLocalProvider(
                    LocalAppActions provides LocalAppActions.current.copy(
                        onShareClick = {
                            shareAction(it)
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
                                onShowColors = { showThemeSelector = true }
                            )
                        }
                    ) {
                        GithubRepo(
                            vm = remember { RepoViewModel(Json.encodeToString(topic)) },
                            backAction = { vm.closeWindow(topic) }
                        )
                    }
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
    onShowColors: () -> Unit,
    refresh: (() -> Unit)? = null,
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
    }
}

class AppViewModel {

    val repoWindows = mutableStateListOf<GitHubTopic>()

    fun newWindow(topic: GitHubTopic) {
        repoWindows.add(topic)
    }

    fun closeWindow(topic: GitHubTopic) {
        repoWindows.remove(topic)
    }

}