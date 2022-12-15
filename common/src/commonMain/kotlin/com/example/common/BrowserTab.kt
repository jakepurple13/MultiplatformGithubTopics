package com.example.common

import androidx.compose.runtime.*

abstract class BrowserTabHandler<T> {
    protected abstract val tabs: MutableList<Tabs.Tab<T>>
    protected abstract val pinnedTabs: MutableList<Tabs.PinnedTab<T>>
    protected abstract var endTabs: Tabs.EndTab<T>?
    abstract var selected: Int

    val tabbed get() = pinnedTabs + tabs + listOfNotNull(endTabs)

    protected abstract fun refreshTab()

    open fun selectedTab() = tabbed.getOrNull(selected)

    open fun newPinnedTab(tab: T) {
        pinnedTabs.add(Tabs.PinnedTab(tab))
    }

    open fun newTab(tab: T) {
        tabs.add(Tabs.Tab(tab))
    }

    open fun hasEndTab(endTab: Boolean) {
        endTabs = if (endTab) {
            Tabs.EndTab()
        } else {
            null
        }
    }

    open fun closePinnedTab(tab: Tabs.PinnedTab<T>) {
        val index = pinnedTabs.indexOf(tab)
        when {
            index < selected -> selected--

            index == selected -> {
                if (pinnedTabs.size > selected) {
                    refreshTab()
                } else {
                    selected--
                }
            }

            index > selected -> Unit
            else -> selected = 0
        }
        pinnedTabs.remove(tab)
    }

    open fun closeTab(tab: Tabs.Tab<T>) {
        val index = tabbed.indexOf(tab)
        when {
            index < selected -> selected--

            index == selected -> {
                if (tabs.size + pinnedTabs.size - 1 > selected) {
                    refreshTab()
                } else {
                    selected--
                }
            }

            index > selected -> Unit
            else -> selected = 0
        }
        tabs.remove(tab)
    }

    open fun nextTab() {
        if (selected == tabs.size + pinnedTabs.size - 1) {
            selected = 0
        } else {
            selected++
        }
    }

    open fun previousTab() {
        if (selected == 0) {
            selected = tabs.size + pinnedTabs.size - 1
        } else {
            selected--
        }
    }

    open fun selectTab(index: Int) {
        selected = index
    }

    open fun pinTab(tab: Tabs.Tab<T>) {
        tabs.remove(tab)
        pinnedTabs.add(tab.pin())
    }

    open fun unpin(tab: Tabs.PinnedTab<T>) {
        pinnedTabs.remove(tab)
        tabs.add(tab.unpin())
    }
}

class BrowserTab<T>(initialTab: Int) : BrowserTabHandler<T>() {
    override val tabs = mutableStateListOf<Tabs.Tab<T>>()
    override val pinnedTabs = mutableStateListOf<Tabs.PinnedTab<T>>()
    override var endTabs by mutableStateOf<Tabs.EndTab<T>?>(null)

    val tabsList by derivedStateOf {
        (pinnedTabs + tabs + listOfNotNull(endTabs))
            .withIndex()
            .toList()
    }

    var refreshKey by mutableStateOf(0)
    override var selected by mutableStateOf(initialTab)

    override fun refreshTab() {
        refreshKey++
    }
}

sealed class Tabs<T> {
    data class PinnedTab<T>(val data: T) : Tabs<T>() {
        fun unpin() = Tab(data)
    }

    data class Tab<T>(val data: T) : Tabs<T>() {
        fun pin() = PinnedTab(data)
    }

    class EndTab<T> : Tabs<T>()
}
