// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.runtime.*
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
        val scope = rememberCoroutineScope()
        var topic by remember { mutableStateOf<GitHubTopic?>(null) }
        var showThemeSelector by remember { mutableStateOf(false) }
        val themeColors by currentThemes.collectAsState(ThemeColors.Default)
        val isDarkMode by isDarkModes.collectAsState(true)

        Theme(
            themeColors = themeColors,
            isDarkMode = isDarkMode,
            appActions = AppActions(
                onCardClick = { topic = it },
                onShareClick = {
                    val stringSelection = StringSelection(it.htmlUrl)
                    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(stringSelection, null)
                },
                onSettingsClick = { showThemeSelector = true }
            )
        ) {
            WindowWithBar(
                windowTitle = "GitHub Topics",
                onCloseRequest = ::exitApplication,
                frameWindowScope = {
                    MenuOptions(
                        isDarkMode = isDarkMode,
                        onModeChange = { scope.launch { db.changeMode(it) } },
                        onShowColors = { showThemeSelector = true }
                    )
                }
            ) { App(remember { TopicViewModel(scope, s) }) }

            if (topic != null) {
                WindowWithBar(
                    windowTitle = topic?.name.orEmpty(),
                    onCloseRequest = { topic = null },
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
                        backAction = { topic = null }
                    )
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
        }
    }
}

@Composable
private fun FrameWindowScope.MenuOptions(
    isDarkMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    onShowColors: () -> Unit
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
    }
}