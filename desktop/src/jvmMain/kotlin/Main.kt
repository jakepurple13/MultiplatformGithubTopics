// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.runtime.*
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.application
import com.example.common.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection


fun main() = application {
    val topic = remember { mutableStateOf<GitHubTopic?>(null) }
    var themeColors by remember { mutableStateOf(ThemeColors.Default) }
    var isDarkMode by remember { mutableStateOf(true) }
    var showThemeSelector by remember { mutableStateOf(false) }

    Theme(
        themeColors = themeColors,
        isDarkMode = isDarkMode,
        appActions = AppActions(
            onCardClick = { topic.value = it },
            onShareClick = {
                val stringSelection = StringSelection(it.htmlUrl)
                val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(stringSelection, null)
            }
        )
    ) {
        WindowWithBar(
            windowTitle = "GitHub Topics",
            onCloseRequest = ::exitApplication,
            frameWindowScope = {
                MenuOptions(
                    isDarkMode = isDarkMode,
                    onModeChange = { isDarkMode = it },
                    onShowColors = { showThemeSelector = true }
                )
            }
        ) {
            val scope = rememberCoroutineScope()
            App(remember { TopicViewModel(scope) })
        }

        if (topic.value != null) {
            WindowWithBar(
                windowTitle = topic.value?.name.orEmpty(),
                onCloseRequest = { topic.value = null },
                frameWindowScope = {
                    MenuOptions(
                        isDarkMode = isDarkMode,
                        onModeChange = { isDarkMode = it },
                        onShowColors = { showThemeSelector = true }
                    )
                }
            ) { GithubRepo(remember { RepoViewModel(Json.encodeToString(topic.value)) }) { topic.value = null } }
        }

        WindowWithBar(
            windowTitle = "Settings",
            onCloseRequest = { showThemeSelector = false },
            visible = showThemeSelector
        ) {
            SettingsScreen(
                currentThemeColors = themeColors,
                setCurrentThemeColors = { themeColors = it },
                isDarkMode = isDarkMode,
                onModeChange = { isDarkMode = it }
            )
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