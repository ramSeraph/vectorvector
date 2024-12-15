package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.metrics.MetricsProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class Processor(
    private val tileRange: TileRange,
    private val sink: TileSink,
    private val metricsProvider: MetricsProvider
) {

    fun process() = runBlocking {
        tileRange.tiles().asFlow()
            .map { tileId ->
                async(Dispatchers.Default) {
                    metricsProvider.get().addCount("Processor.tile")
                    sink.accept(Tile(tileId))
                }
            }
            .flowOn(Dispatchers.Default)
            .map { it.await() }
            .collect()
    }
}