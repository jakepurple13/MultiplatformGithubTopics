package com.example.common.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.common.*
import com.example.common.components.CustomNavigationDrawerItem
import com.example.common.components.IconsButton
import com.example.common.components.InfiniteListHandler
import com.example.common.components.TopicItem
import com.example.common.viewmodels.BaseTopicVM
import com.example.common.viewmodels.FavoritesVM
import kotlinx.coroutines.launch

@Composable
fun App(vm: BaseTopicVM, favoritesVM: FavoritesVM) {
    TopicDrawerLocation(vm, favoritesVM)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GithubTopicUI(
    vm: BaseTopicVM,
    favoritesVM: FavoritesVM,
    navigationIcon: @Composable () -> Unit = {}
) {
    val appActions = LocalAppActions.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val state = LocalMainScrollState.current
    val showButton by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = navigationIcon,
                title = { Text(text = "Github Topics") },
                actions = {
                    Text("Page: ${vm.page}")
                    if (refreshIcon) {
                        IconsButton(
                            onClick = { scope.launch { vm.refresh() } },
                            icon = Icons.Default.Refresh
                        )
                    }
                    IconsButton(
                        onClick = appActions.onSettingsClick,
                        icon = Icons.Default.Settings
                    )
                    AnimatedVisibility(visible = showButton) {
                        IconsButton(
                            onClick = { scope.launch { state.animateScrollToItem(0) } },
                            icon = Icons.Default.ArrowUpward
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        TopicContent(
            modifier = Modifier,
            padding = padding,
            state = state,
            vm = vm,
            onCardClick = appActions.onCardClick,
            favoritesVM = favoritesVM
        )
    }
}

@Composable
fun TopicContent(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    state: LazyListState,
    onCardClick: (GitHubTopic) -> Unit,
    favoritesVM: FavoritesVM,
    vm: BaseTopicVM
) {
    SwipeRefreshWrapper(
        paddingValues = padding,
        isRefreshing = vm.isLoading,
        onRefresh = vm::refresh
    ) {
        Box(
            modifier = modifier
                .padding(padding)
                .padding(vertical = 2.dp)
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
                state = state
            ) {
                items(vm.items) {
                    TopicItemModification(item = it) {
                        TopicItem(
                            item = it,
                            savedTopics = vm.topicList,
                            currentTopics = vm.currentTopics,
                            onCardClick = onCardClick,
                            onTopicClick = vm::addTopic,
                            favoritesVM = favoritesVM
                        )
                    }
                }

                item {
                    val scope = rememberCoroutineScope()
                    ElevatedButton(
                        onClick = { scope.launch { vm.newPage() } },
                        enabled = !vm.isLoading,
                    ) { Text("Load More") }
                }
            }

            ReposScrollBar(state)

            LoadingIndicator(vm)

            if (useInfiniteLoader) {
                InfiniteListHandler(
                    listState = state,
                    onLoadMore = vm::newPage,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun TopicDrawer(vm: BaseTopicVM) {
    var topicText by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            val actions = LocalAppActions.current
            TopAppBar(
                title = { Text("Topics") },
                actions = { IconsButton(onClick = actions.showFavorites, icon = Icons.Default.Favorite) }
            )
        },
        bottomBar = {
            BottomAppBar {
                OutlinedTextField(
                    value = topicText,
                    onValueChange = { topicText = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .onPreviewKeyEvent {
                            if (it.type == KeyEventType.KeyUp) {
                                if (it.key == Key.Enter) {
                                    vm.addTopic(topicText)
                                    topicText = ""
                                    true
                                } else false
                            } else false
                        },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            vm.addTopic(topicText)
                            topicText = ""
                        }
                    ),
                    label = { Text("Enter Topic") },
                    trailingIcon = {
                        IconsButton(
                            onClick = {
                                vm.addTopic(topicText)
                                topicText = ""
                            },
                            icon = Icons.Default.Add
                        )
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 2.dp),
        ) {
            stickyHeader {
                val scope = rememberCoroutineScope()
                ElevatedCard(
                    onClick = { scope.launch { vm.toggleSingleTopic() } }
                ) {
                    ListItem(
                        headlineContent = { Text("Use ${if (vm.singleTopic) "Single" else "Multiple"} Topic(s)") },
                        trailingContent = {
                            Switch(
                                checked = vm.singleTopic,
                                onCheckedChange = { scope.launch { vm.toggleSingleTopic() } }
                            )
                        }
                    )
                }
            }
            items(vm.topicList) {
                CustomNavigationDrawerItem(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    label = { Text(it) },
                    selected = it in vm.currentTopics,
                    onClick = { vm.setTopic(it) },
                    badge = { IconsButton(onClick = { vm.removeTopic(it) }, icon = Icons.Default.Close) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesUsed(backAction: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Libraries Used") },
                navigationIcon = { IconsButton(onClick = backAction, icon = Icons.Default.ArrowBack) },
            )
        }
    ) { LibraryContainer(modifier = Modifier.padding(it).fillMaxSize()) }
}