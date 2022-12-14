package com.example.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.common.viewmodels.BaseTopicVM
import com.example.common.viewmodels.FavoritesVM
import com.example.common.viewmodels.RepoVM

expect fun getPlatformName(): String

expect val refreshIcon: Boolean

expect val useInfiniteLoader: Boolean

@Composable
expect fun TopicItemModification(item: GitHubTopic, content: @Composable () -> Unit)

@Composable
expect fun TopicDrawerLocation(vm: BaseTopicVM, favoritesVM: FavoritesVM)

@Composable
expect fun BoxScope.ReposScrollBar(lazyListState: LazyListState)

@Composable
expect fun BoxScope.ScrollBar(scrollState: ScrollState)

@Composable
expect fun ChipLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit)

@Composable
expect fun BoxScope.LoadingIndicator(vm: BaseTopicVM)

@Composable
expect fun SwipeRefreshWrapper(
    paddingValues: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: suspend () -> Unit,
    content: @Composable () -> Unit
)

@Composable
expect fun M3MaterialThemeSetup(themeColors: ThemeColors, isDarkMode: Boolean, content: @Composable () -> Unit)

@Composable
expect fun MarkdownText(text: String, modifier: Modifier = Modifier)

@Composable
expect fun LibraryContainer(modifier: Modifier = Modifier)

@Composable
expect fun RowScope.RepoViewToggle(repoVM: RepoVM)

@Composable
expect fun RepoContentView(repoVM: RepoVM, modifier: Modifier, defaultContent: @Composable () -> Unit)