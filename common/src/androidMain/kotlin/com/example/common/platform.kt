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

class TopicViewModel : ViewModel(), BaseTopicVM by BaseTopicViewModel() {

    init {
        /*store.data
            .map { it.currentTopicsListList }
            .distinctUntilChanged()
            .onEach {
                currentTopics.clear()
                currentTopics.addAll(it)
            }
            .filter { it.isNotEmpty() && it.all { t -> t.isNotEmpty() } }
            .onEach { refresh() }
            .launchIn(viewModelScope)

        store.data.map { it.topicListList }
            .distinctUntilChanged()
            .onEach {
                topicList.clear()
                topicList.addAll(it)
            }
            .launchIn(viewModelScope)*/
    }

    override fun setTopic(topic: String) {
        viewModelScope.launch {
            currentTopics
            if (topic !in currentTopics) {
                currentTopics.add(topic)
            } else {
                currentTopics.remove(topic)
            }
            if (currentTopics.isNotEmpty()) refresh()
        }
    }

    override fun addTopic(topic: String) {
        if (topic !in topicList && topic.isNotEmpty()) {
            topicList.add(topic)
        }
    }

    override fun removeTopic(topic: String) {
        viewModelScope.launch {
            topicList.remove(topic)
        }
    }
}

class RepoViewModel(t: String) : ViewModel(), RepoVM by BaseRepoViewModel(t)