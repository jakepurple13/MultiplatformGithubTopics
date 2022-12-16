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

class BaseRepoViewModel(
    topic: String
) : RepoVM {
    override val item by lazy { Json.decodeFromString<GitHubTopic>(topic) }
    override var repoContent by mutableStateOf<ReadMeResponse>(ReadMeResponse.Loading)
    override var error by mutableStateOf(false)
    override var showWebView by mutableStateOf(false)

    override suspend fun load() {
        val cached = Cached.cache[item.htmlUrl]
        if (cached != null) {
            println("Loading from cache")
            repoContent = ReadMeResponse.Success(cached.repoContent)
        } else {
            println("Loading from url")
            Network.getReadMe(item.fullName).fold(
                onSuccess = {
                    repoContent = it
                    if (it is ReadMeResponse.Success) Cached.cache[item.htmlUrl] = CachedTopic(item, it.content)
                },
                onFailure = {
                    it.printStackTrace()
                    error = true
                }
            )
        }
    }
}

interface RepoVM {
    val item: GitHubTopic
    var repoContent: ReadMeResponse
    var error: Boolean
    var showWebView: Boolean
    suspend fun load()
}

class BaseFavoritesViewModel(database: Database) : FavoritesVM {
    override val items: SnapshotStateList<GitHubTopic> = mutableStateListOf()
    override val db: FavoritesDatabase by lazy { FavoritesDatabase(database) }
    override fun addFavorite(repo: GitHubTopic) = Unit
    override fun removeFavorite(repo: GitHubTopic) = Unit
}

interface FavoritesVM {
    val items: SnapshotStateList<GitHubTopic>
    val db: FavoritesDatabase

    fun addFavorite(repo: GitHubTopic)
    fun removeFavorite(repo: GitHubTopic)
}