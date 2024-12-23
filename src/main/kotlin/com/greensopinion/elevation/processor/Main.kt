package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.elevation.BlockElevationDataStore
import com.greensopinion.elevation.processor.elevation.CachingBlockStore
import com.greensopinion.elevation.processor.elevation.CachingElevationDataStore
import com.greensopinion.elevation.processor.elevation.Degrees
import com.greensopinion.elevation.processor.elevation.FilesystemBlockStore
import com.greensopinion.elevation.processor.metrics.PeriodicMetrics
import com.greensopinion.elevation.processor.metrics.SingletonMetricsProvider
import com.greensopinion.elevation.processor.sink.CompositeTileSink
import com.greensopinion.elevation.processor.sink.FaultBarrierTileSink
import com.greensopinion.elevation.processor.sink.FilesystemTileRepository
import com.greensopinion.elevation.processor.sink.TerrariumSink
import com.greensopinion.elevation.processor.sink.VectorTileSink
import com.greensopinion.elevation.processor.sink.contour.ContourOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.time.Duration

private val log = KotlinLogging.logger { }

fun main(args: Array<String>) {
    val options = parseCommandLine(args)
    if (options.validateData) {
        validateData(options)
        return
    }
    val tileExtent = 256
    val blockExtent = 6000
    val metricsProvider = SingletonMetricsProvider()
    val dataStore = CachingElevationDataStore(
        BlockElevationDataStore(
            blockSize = Degrees(5.0),
            blockExtent = blockExtent,
            tileExtent = tileExtent,
            blockStore = CachingBlockStore(
                FilesystemBlockStore(blockExtent = blockExtent, folder = options.dataDir!!, metricsProvider = metricsProvider),
                metricsProvider
            )
        )
    )
    val repository = FilesystemTileRepository(outputFolder = options.outputDir!!)
    val sinks = mutableListOf<TileSink>()
    if (options.terrarium) {
        sinks.add(
            TerrariumSink(
                extent = tileExtent,
                repository = repository,
                elevationDataStore = dataStore,
                metricsProvider = metricsProvider
            )
        )
    }
    if (options.vector) {
        sinks.add(
            VectorTileSink(
                contourOptionsProvider = { tile ->
                    if (tile.id.z < 9) {
                        ContourOptions(minorLevel = 100, majorLevel = 200)
                    } else if (tile.id.z < 10) {
                        ContourOptions(minorLevel = 50, majorLevel = 100)
                    } else if (tile.id.z < 12) {
                        ContourOptions(minorLevel = 20, majorLevel = 100)
                    } else
                        ContourOptions(minorLevel = 10, majorLevel = 50)
                },
                repository = repository,
                elevationDataStore = dataStore,
                metricsProvider = metricsProvider
            )
        )
    }
    require(sinks.isNotEmpty()) { "No outputs specified, nothing to do!" }
    PeriodicMetrics(
        interval = Duration.ofSeconds(30),
        metrics = metricsProvider.metrics
    ).use {
        Processor(
            tileRange = options.toTileRange(),
            metricsProvider = metricsProvider,
            sink = FaultBarrierTileSink(CompositeTileSink(sinks))
        ).process()
    }
}

private fun validateData(options: CliOptions) {
    log.info { "Validating data only" }
    val metricsProvider = SingletonMetricsProvider()
    val blockStore = FilesystemBlockStore(
        blockExtent = 6000,
        folder = options.dataDir!!,
        metricsProvider
    )
    blockStore.validateAll()
    log.info { "Done" }
}

private fun parseCommandLine(args: Array<String>): CliOptions {
    val options = CliOptions();
    CommandLine(options).parseArgs(*args)
    return options
}

private fun CliOptions.toTileRange() = TileRange(
    minZ = this.minZ,
    maxZ = this.maxZ,
    minX = this.minX,
    maxX = this.maxX,
    minY = this.minY,
    maxY = this.maxY,
)