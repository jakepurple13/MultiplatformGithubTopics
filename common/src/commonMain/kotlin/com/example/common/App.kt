package com.example.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Immutable
data class AppActions(
    val onCardClick: (GitHubTopic) -> Unit = {},
    val onNewTabOpen: (GitHubTopic) -> Unit = {},
    val onNewWindow: (GitHubTopic) -> Unit = {},
    val onShareClick: (GitHubTopic) -> Unit = {},
    val onSettingsClick: () -> Unit = {},
    val showLibrariesUsed: () -> Unit = {}
)

@Composable
fun App(vm: BaseTopicVM) {
    TopicDrawerLocation(vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GithubTopicUI(vm: BaseTopicVM, navigationIcon: @Composable () -> Unit = {}) {
    val appActions = LocalAppActions.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarScrollState())
    val state = LocalMainScrollState.current
    val showButton by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }

    Scaffold(
        topBar = {
            SmallTopAppBar(
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
            onCardClick = appActions.onCardClick
        )
    }
}

@Composable
fun TopicContent(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    state: LazyListState,
    onCardClick: (GitHubTopic) -> Unit,
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
                            onTopicClick = vm::addTopic
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

@Composable
fun InfiniteListHandler(
    listState: LazyListState,
    buffer: Int = 2,
    onLoadMore: suspend () -> Unit
) {
    val loadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false

            lastVisibleItem.index == listState.layoutInfo.totalItemsCount - buffer
        }
    }

    LaunchedEffect(loadMore) {
        snapshotFlow { loadMore.value }
            .drop(1)
            .collect { if (it) onLoadMore() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicItem(
    item: GitHubTopic,
    savedTopics: List<String>,
    currentTopics: List<String>,
    onCardClick: (GitHubTopic) -> Unit,
    onTopicClick: (String) -> Unit
) {
    val actions = LocalAppActions.current
    OutlinedCard(
        onClick = { onCardClick(item) },
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            ListItem(
                headlineText = { Text(item.name) },
                overlineText = {
                    Text(
                        item.fullName,
                        textDecoration = TextDecoration.Underline
                    )
                },
                supportingText = { item.description?.let { Text(it) } },
                leadingContent = {
                    Surface(shape = CircleShape) {
                        KamelImage(
                            lazyPainterResource(item.owner.avatarUrl.orEmpty()),
                            modifier = Modifier.size(48.dp),
                            contentDescription = null,
                            animationSpec = tween()
                        )
                    }
                },
                trailingContent = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconsButton(onClick = { actions.onShareClick(item) }, icon = Icons.Default.Share)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Text(item.stars.toString())
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ForkLeft, contentDescription = null)
                            Text(item.forks.toString())
                        }
                    }
                }
            )

            ChipLayout(modifier = Modifier.padding(4.dp)) {
                item.topics.forEach {
                    AssistChip(
                        label = { Text(it) },
                        modifier = Modifier.padding(2.dp),
                        onClick = { onTopicClick(it) },
                        leadingIcon = if (it in currentTopics) {
                            { Icon(Icons.Default.CatchingPokemon, null, modifier = Modifier.rotate(180f)) }
                        } else null,
                        border = AssistChipDefaults.assistChipBorder(
                            borderColor = when (it) {
                                in savedTopics -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    )
                }
            }

            item.license?.let {
                Text(
                    it.name,
                    modifier = Modifier.padding(4.dp)
                )
            }

            Row {
                Text(
                    text = item.pushedAt,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
                )

                Text(
                    text = item.language,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f)
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
        topBar = { SmallTopAppBar(title = { Text("Topics") }) },
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
                        headlineText = { Text("Use ${if (vm.singleTopic) "Single" else "Multiple"} Topic(s)") },
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
                NavigationDrawerItem(
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

@Composable
fun IconsButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = IconButton(onClick, modifier, enabled, interactionSource, colors) { Icon(icon, null) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GithubRepo(
    vm: RepoVM,
    backAction: () -> Unit
) {
    val appActions = LocalAppActions.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) { vm.load() }

    if (vm.error) {
        AlertDialog(
            onDismissRequest = { vm.error = false },
            title = { Text("Something went wrong") },
            text = { Text("Something went wrong. Either something happened with the connection or this repo has no readme") },
            confirmButton = { TextButton(onClick = { vm.error = false }) { Text("Dismiss") } }
        )
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                navigationIcon = { IconsButton(onClick = backAction, icon = Icons.Default.ArrowBack) },
                title = {
                    ListItem(
                        headlineText = { Text(vm.item.name, style = MaterialTheme.typography.titleLarge) },
                        overlineText = { Text(vm.item.fullName) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text("Open in Browser") },
                        icon = { Icon(Icons.Default.OpenInBrowser, null) },
                        onClick = { uriHandler.openUri(vm.item.htmlUrl) })
                },
                icons = {
                    NavigationBarItem(
                        selected = false,
                        onClick = { appActions.onShareClick(vm.item) },
                        icon = { Icon(Icons.Default.Share, null) },
                        label = { Text("Share") }
                    )
                }
            )
        }
    ) { padding ->
        Crossfade(targetState = vm.repoContent) { content ->
            when (content) {
                is ReadMeResponse.Failed -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            content.message + "\nThis repo may not have a ReadMe file. Please visit in browser",
                            textAlign = TextAlign.Center
                        )
                    }
                }

                ReadMeResponse.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ReadMeResponse.Success -> {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        MarkdownText(
                            text = content.content,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesUsed(backAction: () -> Unit) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Libraries Used") },
                navigationIcon = { IconsButton(onClick = backAction, icon = Icons.Default.ArrowBack) },
            )
        }
    ) { LibraryContainer(modifier = Modifier.padding(it).fillMaxSize()) }
}