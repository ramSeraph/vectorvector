package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.metrics.MetricsProvider

class CachingBlockStore(
    private val delegate: BlockStore,
    private val metricsProvider: MetricsProvider
) : BlockStore {
    private val maxCacheSize: Int = 9
    private val lock = Any()
    private val cache = object : LinkedHashMap<String, ElevationTile>(99, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ElevationTile>?): Boolean {
            val evict = size > maxCacheSize
            metricsProvider.get().addCount("CachingBlockStore.eviction", evict)
            return evict
        }
    }

    override fun load(blockId: BlockId): ElevationTile {
        val key = "${blockId.x},${blockId.y}"
        synchronized(lock) {
            var tile = cache[key]
            if (tile == null) {
                tile = cache.computeIfAbsent(key) {
                    metricsProvider.get().addCount("CachingBlockStore.miss")
                    metricsProvider.get().addCount("CachingBlockStore.load.${key}")
                    delegate.load(blockId)
                }
            }
            return tile
        }
    }
}