package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.elevation.*
import com.greensopinion.elevation.processor.metrics.DefaultMetrics
import com.greensopinion.elevation.processor.metrics.PeriodicMetrics
import com.greensopinion.elevation.processor.metrics.SingletonMetricsProvider
import com.greensopinion.elevation.processor.sink.TerrariumSink
import picocli.CommandLine
import java.time.Duration

fun main(args: Array<String>) {
    val options = parseCommandLine(args)
    val tileExtent = 256
    val metricsProvider = SingletonMetricsProvider()
    val periodicMetrics = PeriodicMetrics(
        interval = Duration.ofSeconds(10),
        metrics = metricsProvider.metrics
    ).use {
        Processor(
            tileRange = options.toTileRange(),
            metricsProvider = metricsProvider,
            sink = TerrariumSink(
                elevationDataStore = BlockElevationDataStore(
                    blockSize = Degrees(5.0),
                    blockExtent = 6000,
                    tileExtent = tileExtent,
                    blockStore = CachingBlockStore(FilesystemBlockStore(
                        folder = options.dataDir!!
                    ),metricsProvider)
                ),
                extent = tileExtent,
                outputFolder = options.outputDir!!,
                metricsProvider = metricsProvider
            )
        ).process()
    }
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