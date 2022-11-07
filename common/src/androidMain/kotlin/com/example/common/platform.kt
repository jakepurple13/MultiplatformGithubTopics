package com.example.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.flowlayout.FlowRow
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
actual fun ChipLayout(modifier: Modifier, content: @Composable () -> Unit) {
    FlowRow(modifier, content = content)
}

@Composable
actual fun BoxScope.LoadingIndicator(vm: BaseTopicVM) {
    AnimatedVisibility(vm.isLoading, modifier = Modifier.align(Alignment.TopCenter)) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.TopCenter))
    }
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