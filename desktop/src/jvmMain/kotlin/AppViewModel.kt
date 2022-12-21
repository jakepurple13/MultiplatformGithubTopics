import androidx.compose.runtime.*
import com.example.common.BrowserTab
import com.example.common.GitHubTopic
import com.example.common.Tabs

sealed class TabType {
    class Pinned(val index: Int) : TabType()
    class Normal(val topic: GitHubTopic) : TabType()
}

class AppViewModel {
    val browserTab = BrowserTab<TabType>(1).apply {
        newPinnedTab(TabType.Pinned(0))
        newPinnedTab(TabType.Pinned(1))
        hasEndTab(true)
    }
    var showTopicWindow by mutableStateOf(true)
    var showHistory by mutableStateOf(false)
    val repoWindows = mutableStateListOf<GitHubTopic>()
    private val closedTabRepos = mutableStateListOf<GitHubTopic>()
    private val closedWindowRepos = mutableStateListOf<GitHubTopic>()
    private var closedWindowType = WindowType.None
    var selected
        get() = browserTab.selected
        set(value) = browserTab.selectTab(value)

    val closedItems by derivedStateOf {
        mapOf(
            WindowType.Tab to closedTabRepos,
            WindowType.Window to closedWindowRepos
        )
    }

    enum class WindowType { Tab, Window, None }

    val canReopen by derivedStateOf { closedTabRepos.isNotEmpty() || closedWindowRepos.isNotEmpty() }

    fun reopenTabOrWindow() {
        when (closedWindowType) {
            WindowType.Tab -> {
                closedTabRepos.lastOrNull()
                    ?.also(closedTabRepos::remove)
                    ?.let(::newTab)
            }

            WindowType.Window -> {
                closedWindowRepos.lastOrNull()
                    ?.also(closedWindowRepos::remove)
                    ?.let(repoWindows::add)
            }

            WindowType.None -> Unit
        }
        when {
            closedWindowRepos.isEmpty() && closedTabRepos.isEmpty() -> closedWindowType = WindowType.None
            closedTabRepos.isEmpty() -> closedWindowType = WindowType.Window
            closedWindowRepos.isEmpty() -> closedWindowType = WindowType.Tab
        }
    }

    fun reopenTab(topic: GitHubTopic) {
        closedTabRepos.remove(topic)
        newTab(topic)

        when {
            closedWindowRepos.isEmpty() && closedTabRepos.isEmpty() -> closedWindowType = WindowType.None
            closedTabRepos.isEmpty() -> closedWindowType = WindowType.Window
            closedWindowRepos.isEmpty() -> closedWindowType = WindowType.Tab
        }
    }

    fun reopenWindow(topic: GitHubTopic) {
        closedWindowRepos.remove(topic)
        repoWindows.add(topic)

        when {
            closedWindowRepos.isEmpty() && closedTabRepos.isEmpty() -> closedWindowType = WindowType.None
            closedTabRepos.isEmpty() -> closedWindowType = WindowType.Window
            closedWindowRepos.isEmpty() -> closedWindowType = WindowType.Tab
        }
    }

    fun closeWindow(topic: GitHubTopic) {
        repoWindows.remove(topic)
        if (topic !in closedTabRepos && topic !in closedWindowRepos) {
            closedWindowType = WindowType.Window
            closedWindowRepos.add(topic)
        }
    }

    fun newTab(topic: GitHubTopic) {
        if (
            browserTab.tabbed.filterIsInstance<Tabs.Tab<TabType.Normal>>()
                .none { it.data.topic.htmlUrl == topic.htmlUrl }
        ) browserTab.newTab(TabType.Normal(topic))
    }

    fun newTabAndOpen(topic: GitHubTopic) {
        newTab(topic)
    }

    fun closeTab(topic: Tabs.Tab<TabType>) {
        browserTab.closeTab(topic)
        val topicData = (topic.data as TabType.Normal).topic
        if (topicData !in closedTabRepos && topicData !in closedWindowRepos) {
            closedWindowType = WindowType.Tab
            closedTabRepos.add(topicData)
        }
    }

    fun nextTab() {
        showHistory = false
        browserTab.nextTab()
    }

    fun previousTab() {
        showHistory = false
        browserTab.previousTab()
    }

    fun selectTab(index: Int) {
        showHistory = false
        browserTab.selectTab(index)
    }

    fun closeSelectedTab() {
        val b = browserTab.selectedTab()
        if (b is Tabs.Tab<TabType>) browserTab.closeTab(b)
    }

    fun openHistory() {
        showHistory = true
        browserTab.selectTab(browserTab.tabbed.lastIndex)
    }
}
