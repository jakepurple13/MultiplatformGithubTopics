package com.example.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect fun getPlatformName(): String

expect val refreshIcon: Boolean

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