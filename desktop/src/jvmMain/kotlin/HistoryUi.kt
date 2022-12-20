import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.common.GitHubTopic
import com.example.common.LocalAppActions
import com.example.common.ReposScrollBar
import com.example.common.components.IconsButton
import com.example.common.components.TopicItem
import com.example.common.viewmodels.FavoritesVM

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryUi(
    vm: AppViewModel,
    favoritesVM: FavoritesVM,
    backAction: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarScrollState())
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("History") },
                navigationIcon = { IconsButton(onClick = backAction, icon = Icons.Default.ArrowBack) },
                actions = { Text("${vm.closedItems.values.flatten().size}") },
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
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                state = state
            ) {
                stickyHeader {
                    SmallTopAppBar(
                        title = { Text("Tabs") },
                        actions = { Text(vm.closedItems[AppViewModel.WindowType.Tab].orEmpty().size.toString()) }
                    )
                }
                items(vm.closedItems[AppViewModel.WindowType.Tab].orEmpty()) {
                    HistoryItem(
                        item = it,
                        favoritesVM = favoritesVM,
                        onCardClick = { topic ->
                            vm.selected++
                            vm.reopenTab(topic)
                        },
                        modifier = Modifier.animateItemPlacement(),
                        onNewTabClick = vm::newTab,
                        onOpenNewWindowClick = vm.repoWindows::add
                    )
                }
                stickyHeader {
                    SmallTopAppBar(
                        title = { Text("Windows") },
                        actions = { Text(vm.closedItems[AppViewModel.WindowType.Window].orEmpty().size.toString()) }
                    )
                }
                items(vm.closedItems[AppViewModel.WindowType.Window].orEmpty()) {
                    HistoryItem(
                        item = it,
                        favoritesVM = favoritesVM,
                        onCardClick = vm::reopenWindow,
                        modifier = Modifier.animateItemPlacement(),
                        onNewTabClick = vm::newTab,
                        onOpenNewWindowClick = vm.repoWindows::add
                    )
                }
            }
            ReposScrollBar(state)
        }
    }
}

@Composable
fun HistoryItem(
    item: GitHubTopic,
    favoritesVM: FavoritesVM,
    onNewTabClick: (GitHubTopic) -> Unit,
    onOpenNewWindowClick: (GitHubTopic) -> Unit,
    onCardClick: (GitHubTopic) -> Unit,
    modifier: Modifier
) {
    val actions = LocalAppActions.current
    val uriHandler = LocalUriHandler.current
    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem("Open") { onCardClick(item) },
                ContextMenuItem("Open in New Tab") { onNewTabClick(item) },
                ContextMenuItem("Open in New Window") { onOpenNewWindowClick(item) },
                ContextMenuItem("Open in Browser") { uriHandler.openUri(item.htmlUrl) },
                ContextMenuItem("Share") { actions.onShareClick(item) },
            )
        },
    ) {
        TopicItem(
            item = item,
            favoritesVM = favoritesVM,
            savedTopics = emptyList(),
            currentTopics = emptyList(),
            onCardClick = onCardClick,
            onTopicClick = {},
            modifier = modifier
        )
    }
}