package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.metrics.MetricsProvider
import com.greensopinion.elevation.processor.metrics.Progress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class Processor(
    private val tileRange: TileRange,
    private val sink: TileSink,
    private val metricsProvider: MetricsProvider
) {
    private val progress = Progress(tileRange.size)

    fun process() = runBlocking {
        tileRange.tiles().asFlow()
            .map { tileId ->
                async(Dispatchers.IO) {
                    metricsProvider.get().addCount("Processor.tile")
                    sink.accept(Tile(tileId))
                }
            }
            .flowOn(Dispatchers.IO)
            .map {
                val processed = it.await()
                progress.completedOne(processed)
            }
            .collect()
    }
}