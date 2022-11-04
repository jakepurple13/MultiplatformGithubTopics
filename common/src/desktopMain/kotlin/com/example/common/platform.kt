package com.example.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

actual fun getPlatformName(): String {
    return "Desktop"
}

@Composable
actual fun LoadImage(model: Any?, modifier: Modifier) {

}

@Composable
actual fun ChipLayout(modifier: Modifier, content: @Composable () -> Unit) {

}

class TopicViewModel(private val viewModelScope: CoroutineScope) : BaseTopicVM by BaseTopicViewModel() {

    override fun setTopic(topic: String) {
        viewModelScope.launch {
            if (topic !in currentTopics) {
                currentTopics.add(topic)
            } else {
                currentTopics.remove(topic)
            }
            if(currentTopics.isNotEmpty()) refresh()
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