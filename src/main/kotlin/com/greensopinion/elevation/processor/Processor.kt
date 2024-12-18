package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.metrics.MetricsProvider
import com.greensopinion.elevation.processor.metrics.Progress
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

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