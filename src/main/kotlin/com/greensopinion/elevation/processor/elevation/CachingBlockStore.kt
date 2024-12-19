package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.MaterializedTile
import com.greensopinion.elevation.processor.metrics.MetricsProvider
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future

class CachingBlockStore(
    private val delegate: BlockStore,
    private val metricsProvider: MetricsProvider
) : BlockStore {
    private val maxCacheSize: Int = 50
    private val lock = Any()
    private val cache = object : LinkedHashMap<String, Future<ElevationTile>>(99, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Future<ElevationTile>>?): Boolean {
            val evict = size > maxCacheSize
            metricsProvider.get().addCount("CachingBlockStore.eviction", evict)
            return evict
        }
    }
    private val loader = Executors.newFixedThreadPool(4) { r -> Thread(r).also { it.isDaemon = true } }

    override fun load(blockId: BlockId): ElevationTile {
        val key = "${blockId.x},${blockId.y}"
        var tile: Future<ElevationTile>?
        synchronized(lock) {
            tile = cache[key]
            if (tile == null) {
                tile = cache.computeIfAbsent(key) {
                    metricsProvider.get().addCount("CachingBlockStore.miss")
                    metricsProvider.get().addCount("CachingBlockStore.load.${key}")
                    loadAsync(blockId)
                }
            }
        }
        return tile!!.get()
    }

    private fun loadAsync(blockId: BlockId) : Future<ElevationTile> {
        val future = CompletableFuture<ElevationTile>()
        loader.submit {
            try {
                future.complete(MaterializedTile(delegate.load(blockId)))
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
        return future
    }
}