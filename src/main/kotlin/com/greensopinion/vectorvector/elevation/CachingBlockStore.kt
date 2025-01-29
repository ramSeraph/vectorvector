package com.greensopinion.vectorvector.elevation

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.util.ReentrantExecutor
import com.greensopinion.vectorvector.util.newThreadFactory
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future

class CachingBlockStore(
    private val cacheSize: Long,
    private val delegate: BlockStore,
    private val metricsProvider: MetricsProvider,
    private val log: KLogger = KotlinLogging.logger { }
) : BlockStore {
    private val cache = CacheBuilder.newBuilder()
        .maximumSize(cacheSize)
        .build(object : CacheLoader<BlockId, Future<ElevationTile>>() {
            override fun load(key: BlockId) = loadAsync(key)
        })
    private val providesCache = CacheBuilder.newBuilder()
        .maximumSize(5000)
        .build(object : CacheLoader<BlockId, Boolean>() {
            override fun load(key: BlockId) = loadProvides(key)
        })
    private val predictiveLoading: MutableSet<BlockId> = Collections.synchronizedSet(mutableSetOf())
    private val loader = ReentrantExecutor(
        Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors(),
            newThreadFactory("CachingBlockStore-loader", daemon = true)
        )
    )
    override val blockExtent: Int = delegate.blockExtent

    override fun load(blockId: BlockId): ElevationTile {
        metricsProvider.get().addCount("CachingBlockStore.access")
        return cache.get(blockId).get()
    }

    override fun loadAsync(area: List<BlockId>) {
        metricsProvider.get().addCount("CachingBlockStore.loadAsync.area", area.size)
        metricsProvider.get().addCount("CachingBlockStore.loadAsync")
        area.forEach { cache.get(it) }
    }

    override fun provides(blockId: BlockId): Boolean = providesCache.get(blockId)
    private fun loadProvides(blockId: BlockId): Boolean =
        cache.getIfPresent(blockId) != null || delegate.provides(blockId)
    private fun loadAsync(blockId: BlockId): Future<ElevationTile> {
        val future = CompletableFuture<ElevationTile>()
        loader.execute {
                metricsProvider.get().addCount("CachingBlockStore.load")
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
            }
        }
        return future
    }
}