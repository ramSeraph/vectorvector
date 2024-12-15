package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.metrics.MetricsProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class Processor(
    private val tileRange: TileRange,
    private val sink: TileSink,
    private val metricsProvider: MetricsProvider
) {

    fun process() = runBlocking {
        tileRange.tiles().asFlow()
            .map { tileId ->
                async(Dispatchers.IO) {
                    metricsProvider.get().addCount("Processor.tile")
                    sink.accept(Tile(tileId))
                }
            }
            .flowOn(Dispatchers.IO)
            .map { it.await() }
            .collect()
    }
}