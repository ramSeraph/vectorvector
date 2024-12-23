package com.greensopinion.elevation.processor.elevation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.EmptyTile
import com.greensopinion.elevation.processor.metrics.MetricsProvider
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger

class CachingBlockStore(
    private val delegate: BlockStore,
    private val metricsProvider: MetricsProvider,
    private val log: KLogger = KotlinLogging.logger { }
) : BlockStore {
    private val cache = CacheBuilder.newBuilder()
        .maximumSize(25)
        .build(object : CacheLoader<BlockId, Future<ElevationTile>>() {
            override fun load(key: BlockId) = loadAsync(key)
        })
    private val predictiveLoading: MutableSet<BlockId> = Collections.synchronizedSet(mutableSetOf())
    private val loader = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()) { r ->
        Thread(r).also {
            it.isDaemon = true
            it.name = "CachingBlockStore-loader-${threadIdSeed.incrementAndGet()}"
        }
    }

    override fun load(blockId: BlockId): ElevationTile {
        if (blockId.valid) {
            metricsProvider.get().addCount("CachingBlockStore.access")
        }
        return cache.get(blockId).get()
    }

    private fun loadAsync(blockId: BlockId): Future<ElevationTile> {
        val future = CompletableFuture<ElevationTile>()
        loader.submit {
            if (blockId.valid) {
                metricsProvider.get().addCount("CachingBlockStore.load")
            }
            val loadingPrediction = predictiveLoading.contains(blockId)
            if (loadingPrediction) {
                log.info { "loading $blockId predictively" }
            }
            var tile: ElevationTile? = null
            try {
                tile = delegate.load(blockId).materialize()
                future.complete(tile)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            } finally {
                val prediction = BlockId(blockId.x + 1, blockId.y)
                if (tile != null && !tile.empty && !loadingPrediction && prediction.valid) {
                    predictiveLoading.add(prediction)
                    loader.submit {
                        try {
                            load(prediction)
                        } finally {
                            predictiveLoading.remove(prediction)
                        }
                    }
                }
            }
        }
        return future
    }
}

private val threadIdSeed = AtomicInteger()