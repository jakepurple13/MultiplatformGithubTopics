package com.example.common

import android.os.Build
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

actual fun getPlatformName(): String {
    return "Android"
}

actual val refreshIcon = false

@Composable
actual fun BoxScope.ReposScrollBar(lazyListState: LazyListState) {
}

@Composable
actual fun BoxScope.ScrollBar(scrollState: ScrollState) {
}

@Composable
actual fun M3MaterialThemeSetup(themeColors: ThemeColors, isDarkMode: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (themeColors == ThemeColors.Default) when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> ThemeColors.Default.getThemeScheme(isDarkMode)
    } else themeColors.getThemeScheme(isDarkMode)
    MaterialTheme(colorScheme = colorScheme.animate(), content = content)
}

@Composable
actual fun ChipLayout(modifier: Modifier, content: @Composable () -> Unit) {
    FlowRow(modifier, content = content)
}

@Composable
actual fun SwipeRefreshWrapper(
    paddingValues: PaddingValues,
    isRefreshing: Boolean,
    onRefresh: suspend () -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { scope.launch { onRefresh() } },
        indicatorPadding = paddingValues,
        content = content
    )
}

@Composable
actual fun BoxScope.LoadingIndicator(vm: BaseTopicVM) {
}

class TopicViewModel(s: Flow<SettingInformation>) : ViewModel(), BaseTopicVM by BaseTopicViewModel() {

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

class RepoViewModel(t: String) : ViewModel(), RepoVM by BaseRepoViewModel(t)