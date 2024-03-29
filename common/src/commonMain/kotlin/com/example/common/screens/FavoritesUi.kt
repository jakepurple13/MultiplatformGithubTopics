package com.example.common.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.common.GitHubTopic
import com.example.common.LocalAppActions
import com.example.common.ReposScrollBar
import com.example.common.components.IconsButton
import com.example.common.components.TopicItem
import com.example.common.viewmodels.FavoritesVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesUi(
    favoritesVM: FavoritesVM,
    backAction: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = { IconsButton(onClick = backAction, icon = Icons.Default.ArrowBack) },
                actions = { Text("${favoritesVM.items.size} favorites") },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        val state = rememberLazyListState()
        Box(
            modifier = Modifier
                .padding(padding)
                .padding(vertical = 2.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = state
            ) { items(favoritesVM.items) { FavoriteItem(it, favoritesVM) } }
            ReposScrollBar(state)
        }
    }
}

@Composable
fun FavoriteItem(
    item: GitHubTopic,
    favoritesVM: FavoritesVM
) {
    TopicItem(
        item = item,
        favoritesVM = favoritesVM,
        savedTopics = emptyList(),
        currentTopics = emptyList(),
        onCardClick = LocalAppActions.current.onCardClick,
        onTopicClick = {}
    )
}