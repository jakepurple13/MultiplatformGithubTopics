package com.example.common

import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

data class CachedTopic(val topic: GitHubTopic, val repoContent: String)

object Cached {

    private val map = mutableMapOf<String, CachedTopic>()

    val cache = ExpirableLRUCache<String, CachedTopic>(
        minimalSize = 10,
        flushInterval = TimeUnit.MINUTES.toMillis(5)
    ) {
        size = map.size
        set { key, value -> map[key] = value }
        get { map[it] }
        remove { map.remove(it) }
        clear { map.clear() }
    }

}

@DslMarker
annotation class GenericCacheMarker

/**
 * A Generic K,V [GenericCache] defines the basic operations to a cache.
 */
interface GenericCache<K, V> {
    /**
     * The number of the items that are currently cached.
     */
    val size: Int

    /**
     * Cache a [value] with a given [key]
     */
    operator fun set(key: K, value: V)

    /**
     * Get the cached value of a given [key], or null if it's not cached or evicted.
     */
    operator fun get(key: K): V?

    /**
     * Remove the value of the [key] from the cache, and return the removed value, or null if it's not cached at all.
     */
    fun remove(key: K): V?

    /**
     * Remove all the items in the cache.
     */
    fun clear()

    class GenericCacheBuilder<K, V> {
        @GenericCacheMarker
        var size: Int by Delegates.notNull()
        private var set: (key: K, value: V) -> Unit by Delegates.notNull()
        private var get: (key: K) -> V? by Delegates.notNull()
        private var remove: (key: K) -> V? by Delegates.notNull()
        private var clear: () -> Unit by Delegates.notNull()

        @GenericCacheMarker
        fun set(block: (key: K, value: V) -> Unit) {
            set = block
        }

        @GenericCacheMarker
        fun get(block: (key: K) -> V?) {
            get = block
        }

        @GenericCacheMarker
        fun remove(block: (key: K) -> V?) {
            remove = block
        }

        @GenericCacheMarker
        fun clear(block: () -> Unit) {
            clear = block
        }

        internal fun build() = object : GenericCache<K, V> {
            override val size: Int get() = this@GenericCacheBuilder.size
            override fun set(key: K, value: V) = this@GenericCacheBuilder.set(key, value)
            override fun get(key: K): V? = this@GenericCacheBuilder.get(key)
            override fun remove(key: K): V? = this@GenericCacheBuilder.remove(key)
            override fun clear() = this@GenericCacheBuilder.clear()
        }
    }
}

/**
 * [ExpirableLRUCache] flushes items that are **Least Recently Used** and keeps [minimalSize] items at most
 * along with flushing the items whose life time is longer than [flushInterval].
 */
class ExpirableLRUCache<K, V>(
    private val minimalSize: Int = DEFAULT_SIZE,
    private val flushInterval: Long = TimeUnit.MINUTES.toMillis(1),
    private val delegate: GenericCache<K, V>,
) : GenericCache<K, V> by delegate {

    constructor(
        minimalSize: Int,
        flushInterval: Long,
        delegate: GenericCache.GenericCacheBuilder<K, V>.() -> Unit
    ) : this(
        minimalSize,
        flushInterval,
        GenericCache.GenericCacheBuilder<K, V>().apply(delegate).build()
    )

    private val keyMap = object : LinkedHashMap<K, Boolean>(minimalSize, .75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, Boolean>): Boolean {
            val tooManyCachedItems = size > minimalSize
            if (tooManyCachedItems) eldestKeyToRemove = eldest.key
            return tooManyCachedItems
        }
    }

    private var lastFlushTime = System.nanoTime()

    private var eldestKeyToRemove: K? = null

    override val size: Int
        get() {
            recycle()
            return delegate.size
        }

    override fun set(key: K, value: V) {
        recycle()
        delegate[key] = value
        cycleKeyMap(key)
    }

    override fun remove(key: K): V? {
        recycle()
        return delegate.remove(key)
    }

    override fun get(key: K): V? {
        recycle()
        keyMap[key]
        return delegate[key]
    }

    override fun clear() {
        keyMap.clear()
        delegate.clear()
    }

    private fun cycleKeyMap(key: K) {
        keyMap[key] = PRESENT
        eldestKeyToRemove?.let { delegate.remove(it) }
        eldestKeyToRemove = null
    }

    private fun recycle() {
        val shouldRecycle = System.nanoTime() - lastFlushTime >= TimeUnit.MILLISECONDS.toNanos(flushInterval)
        if (shouldRecycle) {
            delegate.clear()
            lastFlushTime = System.nanoTime()
        }
    }

    companion object {
        private const val DEFAULT_SIZE = 100
        private const val PRESENT = true
    }
}