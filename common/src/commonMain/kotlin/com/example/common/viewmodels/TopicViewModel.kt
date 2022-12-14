package com.example.common.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.common.Database
import com.example.common.GitHubTopic
import com.example.common.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BaseTopicViewModel : BaseTopicVM {

    override val items = mutableStateListOf<GitHubTopic>()
    override var isLoading by mutableStateOf(true)
    override var singleTopic by mutableStateOf(true)
    override val currentTopics = mutableStateListOf<String>()
    override val topicList = mutableStateListOf<String>()
    override var page by mutableStateOf(1)

    override val db: Database by lazy { Database() }

    private suspend fun loadTopics() {
        isLoading = true
        withContext(Dispatchers.IO) {
            Network.getTopics(page, *currentTopics.toTypedArray()).fold(
                onSuccess = { items.addAll(it) },
                onFailure = { it.printStackTrace() }
            )
        }
        isLoading = false
    }

    override suspend fun refresh() {
        items.clear()
        page = 1
        loadTopics()
    }

    override suspend fun newPage() {
        page++
        loadTopics()
    }

}

interface BaseTopicVM {
    fun setTopic(topic: String) = Unit
    fun addTopic(topic: String) = Unit
    fun removeTopic(topic: String) = Unit
    suspend fun refresh() = Unit
    suspend fun newPage() = Unit
    suspend fun toggleSingleTopic() = Unit
    val db: Database

    val items: SnapshotStateList<GitHubTopic>
    var isLoading: Boolean
    val currentTopics: SnapshotStateList<String>
    val topicList: SnapshotStateList<String>
    val page: Int
    var singleTopic: Boolean
}