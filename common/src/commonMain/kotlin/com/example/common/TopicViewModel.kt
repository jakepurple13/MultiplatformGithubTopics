package com.example.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class BaseTopicViewModel : BaseTopicVM {

    override val items = mutableStateListOf<GitHubTopic>()
    override var isLoading by mutableStateOf(true)
    override val currentTopics = mutableStateListOf<String>()
    override val topicList = mutableStateListOf<String>()
    private var page = 1

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

    val items: SnapshotStateList<GitHubTopic>
    var isLoading: Boolean
    val currentTopics: SnapshotStateList<String>
    val topicList: SnapshotStateList<String>
}

class BaseRepoViewModel(
    topic: String
) : RepoVM {
    override val item by lazy { Json.decodeFromString<GitHubTopic>(topic) }
    override var repoContent by mutableStateOf<ReadMeResponse>(ReadMeResponse.Loading)
    override var error by mutableStateOf(false)

    override suspend fun load() {
        Network.getReadMe(item.fullName).fold(
            onSuccess = { repoContent = it },
            onFailure = {
                it.printStackTrace()
                error = true
            }
        )
    }
}

interface RepoVM {
    val item: GitHubTopic
    var repoContent: ReadMeResponse
    var error: Boolean
    suspend fun load()
}