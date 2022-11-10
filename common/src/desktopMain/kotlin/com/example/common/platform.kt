package com.example.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.markdown.Markdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


actual fun getPlatformName(): String {
    return "Desktop"
}

actual val refreshIcon = true

actual val useInfiniteLoader = false

@Composable
actual fun TopicItemModification(item: GitHubTopic, content: @Composable () -> Unit) {
    val actions = LocalAppActions.current
    val uriHandler = LocalUriHandler.current
    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem("Open") { actions.onCardClick(item) },
                ContextMenuItem("Open in Browser") { uriHandler.openUri(item.htmlUrl) },
                ContextMenuItem("Share") { actions.onShareClick(item) },
            )
        },
        content = content
    )
}

@Composable
actual fun LibraryContainer(modifier: Modifier) {
    LibrariesContainer(
        useResource("aboutlibraries.json") { it.bufferedReader().readText() },
        modifier = modifier
    )
}

@Composable
actual fun M3MaterialThemeSetup(themeColors: ThemeColors, isDarkMode: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = themeColors.getThemeScheme(isDarkMode).animate(), content = content)
}

@Composable
actual fun BoxScope.ReposScrollBar(lazyListState: LazyListState) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(lazyListState),
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .padding(end = 4.dp)
    )
}

@Composable
actual fun BoxScope.ScrollBar(scrollState: ScrollState) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
    )
}

@Composable
actual fun ChipLayout(modifier: Modifier, content: @Composable () -> Unit) {
    FlowRow(modifier = modifier, content = content)
}

@Composable
actual fun SwipeRefreshWrapper(
    paddingValues: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: suspend () -> Unit,
    content: @Composable () -> Unit
) = content()

@Composable
actual fun BoxScope.LoadingIndicator(vm: BaseTopicVM) {
    AnimatedVisibility(vm.isLoading, modifier = Modifier.align(Alignment.TopCenter)) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
actual fun MarkdownText(text: String, modifier: Modifier) {
    Markdown(
        content = text,
        modifier = modifier
    )
}

class TopicViewModel(private val viewModelScope: CoroutineScope, s: Flow<SettingInformation>) :
    BaseTopicVM by BaseTopicViewModel() {

    init {
        s
            .map { it.topicList }
            .distinctUntilChanged()
            .onEach {
                topicList.clear()
                topicList.addAll(it)
            }
            .launchIn(viewModelScope)

        s
            .map { it.currentTopics }
            .distinctUntilChanged()
            .onEach {
                currentTopics.clear()
                currentTopics.addAll(it)
                if (it.isNotEmpty()) refresh()
            }
            .launchIn(viewModelScope)
    }

    override fun setTopic(topic: String) {
        viewModelScope.launch {
            if (topic !in currentTopics) {
                db.addCurrentTopic(topic)
            } else {
                db.removeCurrentTopic(topic)
            }
        }
    }

    override fun addTopic(topic: String) {
        viewModelScope.launch {
            if (topic !in topicList) {
                db.addTopic(topic)
            }
        }
    }

    override fun removeTopic(topic: String) {
        viewModelScope.launch {
            db.removeTopic(topic)
        }
    }
}

class RepoViewModel(t: String) : RepoVM by BaseRepoViewModel(t)