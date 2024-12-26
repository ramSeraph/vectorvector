package com.greensopinion.vectorvector

import com.greensopinion.vectorvector.cli.CliOptions
import com.greensopinion.vectorvector.cli.CliOutputFormat
import com.greensopinion.vectorvector.elevation.BlockElevationDataStore
import com.greensopinion.vectorvector.elevation.CachingBlockStore
import com.greensopinion.vectorvector.elevation.CachingElevationDataStore
import com.greensopinion.vectorvector.elevation.Degrees
import com.greensopinion.vectorvector.elevation.FilesystemBlockStore
import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.metrics.PeriodicMetrics
import com.greensopinion.vectorvector.metrics.SingletonMetricsProvider
import com.greensopinion.vectorvector.sink.CompositeTileSink
import com.greensopinion.vectorvector.repository.FilesystemTileRepository
import com.greensopinion.vectorvector.repository.MbtilesMetadata
import com.greensopinion.vectorvector.repository.MbtilesTileRepository
import com.greensopinion.vectorvector.sink.TerrariumSink
import com.greensopinion.vectorvector.repository.TileRepository
import com.greensopinion.vectorvector.sink.VectorTileSink
import com.greensopinion.vectorvector.sink.contour.ContourOptions
import com.greensopinion.vectorvector.sink.vectorSchema
import com.greensopinion.vectorvector.util.closeSafely
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.io.File
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
    val dataStore = cachingElevationDataStore(options, blockExtent, tileExtent, metricsProvider)

    val sinkToRepository = tileSinkToRepository(
        options,
        tileExtent,
        dataStore,
        metricsProvider,
        tileRepositoryFactory(options, metricsProvider)
    )
    try {
        val sink = CompositeTileSink(sinkToRepository.keys.toList())
        PeriodicMetrics(
            interval = Duration.ofSeconds(30),
            metrics = metricsProvider.metrics
        ).use {
            Processor(
                tileRange = options.toTileRange(),
                metricsProvider = metricsProvider,
                sink = sink
            ).process()
        }
    } finally {
        closeSafely(sinkToRepository.values)
    }
}

private fun tileRepositoryFactory(
    options: CliOptions,
    metricsProvider: SingletonMetricsProvider
): (name: String, format: String) -> TileRepository =
    if (options.outputFormat == CliOutputFormat.mbtiles) {
        { name, format ->
            createMbTilesRepository(
                options,
                format,
                File(options.outputDir, "$name.mbtiles"),
                metricsProvider
            )
        }
    } else {
        { _, _ -> FilesystemTileRepository(options.outputDir) }
    }

private fun cachingElevationDataStore(
    options: CliOptions,
    blockExtent: Int,
    tileExtent: Int,
    metricsProvider: SingletonMetricsProvider
) = CachingElevationDataStore(
    BlockElevationDataStore(
        blockSize = Degrees(5.0),
        blockExtent = blockExtent,
        tileExtent = tileExtent,
        blockStore = CachingBlockStore(
            FilesystemBlockStore(
                blockExtent = blockExtent,
                folder = options.dataDir!!,
                metricsProvider = metricsProvider
            ),
            metricsProvider
        )
    )
)

private fun tileSinkToRepository(
    options: CliOptions,
    tileExtent: Int,
    dataStore: CachingElevationDataStore,
    metricsProvider: SingletonMetricsProvider,
    repositoryFactory: (name: String, format: String) -> TileRepository
): Map<TileSink, TileRepository> {
    val sinks = mutableMapOf<TileSink, TileRepository>()
    if (options.terrarium) {
        val repository = repositoryFactory("terrarium", "png")
        sinks[TerrariumSink(
            extent = tileExtent,
            repository = repository,
            elevationDataStore = dataStore,
            metricsProvider = metricsProvider
        )] = repository
    }
    if (options.vector) {
        val repository = repositoryFactory("vector", "pbf")
        sinks[VectorTileSink(
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
        )] = repository
    }
    require(sinks.isNotEmpty()) { "No outputs specified, nothing to do!" }
    return sinks
}

fun createMbTilesRepository(
    options: CliOptions,
    format: String,
    file: File,
    metricsProvider: MetricsProvider
): TileRepository =
    MbtilesTileRepository(file, metricsProvider).also { repository ->
        val contourOptions = ContourOptions()
        try {
            repository.setMetadata(
                MbtilesMetadata(
                    name = "VectorVector $format tiles",
                    format = format,
                    json = if (format == "pbf") vectorSchema(
                        minZoom = options.minZ,
                        maxZoom = options.maxZ,
                        contourLayer = contourOptions.contourLayer,
                        levelName = contourOptions.levelKey,
                        elevationName = contourOptions.elevationKey
                    ) else "",
                    minZoom = options.minZ,
                    maxZoom = options.maxZ
                )
            )
        } catch (e: Exception) {
            repository.close()
            throw e
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
    options.area?.bounds?.applyTo(options)
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