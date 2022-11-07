package com.example.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import com.mikepenz.markdown.Markdown
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch


@Immutable
data class AppActions(
    val onCardClick: (GitHubTopic) -> Unit,
    val onShareClick: (GitHubTopic) -> Unit
)

@Composable
fun App(vm: BaseTopicVM) {
    GithubTopicUI(vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GithubTopicUI(vm: BaseTopicVM) {
    val appActions = LocalAppActions.current
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarScrollState())
    val state = rememberLazyListState()
    val showButton by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerContent = { TopicDrawer(vm) },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconsButton(
                            onClick = { scope.launch { drawerState.open() } },
                            icon = Icons.Default.Menu
                        )
                    },
                    title = { Text(text = "Github Topics") },
                    actions = {
                        if (refreshIcon) {
                            IconsButton(
                                onClick = { scope.launch { vm.refresh() } },
                                icon = Icons.Default.Refresh
                            )
                        }
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
}

@Composable
fun TopicContent(
    modifier: Modifier = Modifier,
    padding: PaddingValues,
    state: LazyListState,
    onCardClick: (GitHubTopic) -> Unit,
    vm: BaseTopicVM
) {
    Box(
        modifier = modifier
            .padding(padding)
            .padding(vertical = 2.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize(),
            state = state
        ) {
            items(vm.items) {
                TopicItem(
                    item = it,
                    savedTopics = vm.topicList,
                    currentTopics = vm.currentTopics,
                    onCardClick = onCardClick,
                    onTopicClick = vm::addTopic
                )
            }
        }

        ReposScrollBar(state)

        LoadingIndicator(vm)

        InfiniteListHandler(
            listState = state,
            onLoadMore = vm::newPage
        )
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
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - buffer)
        }
    }

    LaunchedEffect(loadMore) {
        snapshotFlow { loadMore.value }
            .drop(1)
            .distinctUntilChanged()
            .collect { onLoadMore() }
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
    OutlinedCard(
        onClick = { onCardClick(item) }
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
                            contentDescription = null
                        )
                    }
                },
                trailingContent = {
                    Column {
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
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
            items(vm.topicList) {
                NavigationDrawerItem(
                    modifier = Modifier.padding(horizontal = 2.dp),
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
    vm: RepoViewModel,
    backAction: () -> Unit
) {
    val appActions = LocalAppActions.current
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarScrollState())

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
                actions = {
                    var showDropDownMenu by remember { mutableStateOf(false) }

                    DropdownMenu(expanded = showDropDownMenu, onDismissRequest = { showDropDownMenu = false }) {
                        DropdownMenuItem(
                            content = {
                                Icon(Icons.Default.OpenInBrowser, null)
                                Text("Open in Browser")
                            },
                            onClick = {
                                showDropDownMenu = false
                                uriHandler.openUri(vm.item.htmlUrl)
                            }
                        )

                        DropdownMenuItem(
                            content = {
                                Icon(Icons.Default.Share, null)
                                Text("Share")
                            },
                            onClick = {
                                showDropDownMenu = false
                                appActions.onShareClick(vm.item)
                            }
                        )
                    }

                    IconsButton(onClick = { showDropDownMenu = true }, icon = Icons.Default.MoreVert)
                },
                scrollBehavior = scrollBehavior
            )
        },
        /*bottomBar = {
            BottomAppBar(
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text("Open in Browser") },
                        icon = { Icon(Icons.Default.OpenInBrowser, null) },
                        onClick = { uriHandler.openUri(vm.item.htmlUrl) })
                },
                actions = {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, vm.item.htmlUrl)
                                putExtra(Intent.EXTRA_TITLE, vm.item.name)
                                type = "text/plain"
                            }

                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        icon = { Icon(Icons.Default.Share, null) },
                        label = { Text("Share") }
                    )
                }
            )
        },*/
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
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
                        Markdown(
                            content = content.content,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}