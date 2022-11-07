// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.common.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        val topic = remember { mutableStateOf<GitHubTopic?>(null) }

        Theme(
            appActions = AppActions(
                onCardClick = { topic.value = it },
                onShareClick = {
                    val stringSelection = StringSelection(it.htmlUrl)
                    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(stringSelection, null)
                }
            )
        ) {
            val scope = rememberCoroutineScope()
            App(remember { TopicViewModel(scope) })

            if (topic.value != null) {
                Window(onCloseRequest = { topic.value = null }) {
                    GithubRepo(remember { RepoViewModel(Json.encodeToString(topic.value)) }) { topic.value = null }
                }
            }
        }
    }
}
