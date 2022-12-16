package com.example.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.markdown.Markdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import java.awt.Cursor


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
                ContextMenuItem("Open in New Tab") { actions.onNewTabOpen(item) },
                ContextMenuItem("Open in New Window") { actions.onNewWindow(item) },
                ContextMenuItem("Open in Browser") { uriHandler.openUri(item.htmlUrl) },
                ContextMenuItem("Share") { actions.onShareClick(item) },
            )
        },
        content = content
    )
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
actual fun TopicDrawerLocation(vm: BaseTopicVM, favoritesVM: FavoritesVM) {
    val splitter = rememberSplitPaneState()

    HorizontalSplitPane(splitPaneState = splitter) {
        first(250.dp) { TopicDrawer(vm) }
        second(550.dp) { GithubTopicUI(vm, favoritesVM) }

        splitter {
            visiblePart {
                Box(
                    Modifier.width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.onBackground)
                )
            }
            handle {
                Box(
                    Modifier
                        .markAsHandle()
                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))
                        .background(SolidColor(Color.Gray), alpha = 0.50f)
                        .width(2.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
actual fun LibraryContainer(modifier: Modifier) {
    val uriHandler = LocalUriHandler.current
    LibrariesContainer(
        useResource("aboutlibraries.json") { it.bufferedReader().readText() },
        modifier = modifier,
        onLibraryClick = { it.website?.let(uriHandler::openUri) },
        colors = LibraryDefaults.libraryColors(
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            badgeBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            badgeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
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

@Composable
actual fun RowScope.RepoViewToggle(repoVM: RepoVM) {
    NavigationBarItem(
        selected = repoVM.showWebView,
        onClick = { repoVM.showWebView = !repoVM.showWebView },
        icon = {
            Icon(
                if (repoVM.showWebView) Icons.Default.WebAsset else Icons.Default.WebAssetOff,
                null
            )
        },
        label = { Text("Show WebView") }
    )

    AnimatedVisibility(
        repoVM.showWebView,
        modifier = Modifier.weight(1f)
    ) {
        val browser = (repoVM as RepoViewModel).browser
        Row {
            NavigationBarItem(
                selected = false,
                onClick = { browser.goBack() },
                icon = { Icon(Icons.Default.ArrowCircleLeft, null) },
                label = { Text("Back") },
                enabled = repoVM.showWebView
            )

            NavigationBarItem(
                selected = false,
                onClick = { browser.goForward() },
                icon = { Icon(Icons.Default.ArrowCircleRight, null) },
                label = { Text("Forward") },
                enabled = repoVM.showWebView
            )
        }
    }
}

@Composable
actual fun RepoContentView(repoVM: RepoVM, modifier: Modifier, defaultContent: @Composable () -> Unit) {
    Crossfade(repoVM.showWebView) { target ->
        when (target) {
            true -> WebView((repoVM as RepoViewModel).browser.uiComponent, modifier)
            false -> defaultContent()
        }
    }
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

        s
            .map { it.singleTopic }
            .distinctUntilChanged()
            .onEach { singleTopic = it }
            .launchIn(viewModelScope)
    }

    override fun setTopic(topic: String) {
        viewModelScope.launch {
            if (singleTopic) {
                db.setCurrentTopic(topic)
            } else {
                if (topic !in currentTopics) {
                    db.addCurrentTopic(topic)
                } else {
                    db.removeCurrentTopic(topic)
                }
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

    override suspend fun toggleSingleTopic() {
        db.singleTopicToggle(!singleTopic)
    }
}

class RepoViewModel(t: String, browserHandler: BrowserHandler) : RepoVM by BaseRepoViewModel(t) {
    val browser by lazy { browserHandler.createBrowser(item.htmlUrl) }
}

class FavoritesViewModel(
    private val viewModelScope: CoroutineScope,
    database: Database
) : FavoritesVM by BaseFavoritesViewModel(database) {
    init {
        viewModelScope.launch {
            db.favoriteRepos()
                .distinctUntilChanged()
                .onEach {
                    items.clear()
                    items.addAll(it)
                }
                .collect()
        }
    }

    override fun addFavorite(repo: GitHubTopic) {
        viewModelScope.launch { db.addFavorite(repo) }
    }

    override fun removeFavorite(repo: GitHubTopic) {
        viewModelScope.launch { db.removeFavorite(repo) }
    }
}