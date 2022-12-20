package com.example.common.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.common.Database
import com.example.common.FavoritesDatabase
import com.example.common.GitHubTopic

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