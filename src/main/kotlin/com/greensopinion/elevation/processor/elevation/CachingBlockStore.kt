package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.ElevationTile

class CachingBlockStore(
    private val delegate: BlockStore
) : BlockStore {
    private val maxCacheSize: Int = 9
    private val cache = object : LinkedHashMap<String, ElevationTile>(99, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ElevationTile>?): Boolean {
            return size > maxCacheSize
        }
    }

    override fun load(blockId: BlockId): ElevationTile {
        val key = "${blockId.x},${blockId.y}"
        return cache[key] ?: cache.computeIfAbsent(key) { delegate.load(blockId) }
    }
}