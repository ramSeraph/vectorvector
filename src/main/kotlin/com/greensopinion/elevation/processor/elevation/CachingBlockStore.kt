package com.greensopinion.elevation.processor.elevation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.metrics.MetricsProvider
import java.io.Closeable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future

class CachingBlockStore(
    private val delegate: BlockStore,
    private val metricsProvider: MetricsProvider
) : BlockStore {
    private val cache = CacheBuilder.newBuilder().maximumSize(50)
            .build(object : CacheLoader<BlockId, Future<ElevationTile>>() {
                override fun load(key: BlockId) = loadAsync(key)
            })
    private val loader = Executors.newFixedThreadPool(4) { r -> Thread(r).also { it.isDaemon = true } }

    override fun load(blockId: BlockId): ElevationTile {
        return cache.get(blockId).get()
    }

    private fun loadAsync(blockId: BlockId): Future<ElevationTile> {
        val future = CompletableFuture<ElevationTile>()
        loader.submit {
            try {
                future.complete(delegate.load(blockId).materialize())
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
        return future
    }
}