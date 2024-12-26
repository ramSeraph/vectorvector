package com.greensopinion.vectorvector

import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.metrics.Progress
import com.greensopinion.vectorvector.metrics.toLogString
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val metricsProvider: MetricsProvider,
    private val log :KLogger =  KotlinLogging.logger {  }
) {
    private val progress = Progress(tileRange.size)

    fun process() = runBlocking {
        log.info { "Processing area of z=${tileRange.minZ}-${tileRange.maxZ} x=${tileRange.minX}-${tileRange.maxX} y=${tileRange.minY}-${tileRange.maxY} (${tileRange.size} tiles)" }
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
        log.info { "Completed in ${progress.elapsed.toLogString()}" }
    }
}