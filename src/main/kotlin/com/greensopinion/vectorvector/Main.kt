package com.greensopinion.vectorvector

import com.greensopinion.vectorvector.cli.CliOptions
import com.greensopinion.vectorvector.cli.CliOutputFormat
import com.greensopinion.vectorvector.cli.DataSourceFormat
import com.greensopinion.vectorvector.elevation.Aw3d30ElevationDataStore
import com.greensopinion.vectorvector.elevation.Carto30ElevationDataStore
import com.greensopinion.vectorvector.elevation.BlockElevationDataStore
import com.greensopinion.vectorvector.elevation.BlockOffsetMapper
import com.greensopinion.vectorvector.elevation.BlockStore
import com.greensopinion.vectorvector.elevation.CachingBlockStore
import com.greensopinion.vectorvector.elevation.CachingElevationDataStore
import com.greensopinion.vectorvector.elevation.DegreeBlockMapper
import com.greensopinion.vectorvector.elevation.Degrees
import com.greensopinion.vectorvector.elevation.ElevationDataStore
import com.greensopinion.vectorvector.elevation.SrtmBlockMapper
import com.greensopinion.vectorvector.elevation.SrtmBlockStore
import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.metrics.PeriodicMetrics
import com.greensopinion.vectorvector.metrics.SingletonMetricsProvider
import com.greensopinion.vectorvector.repository.FilesystemTileRepository
import com.greensopinion.vectorvector.repository.MbtilesMetadata
import com.greensopinion.vectorvector.repository.MbtilesTileRepository
import com.greensopinion.vectorvector.repository.TileRepository
import com.greensopinion.vectorvector.sink.CompositeTileSink
import com.greensopinion.vectorvector.sink.HillshadeRasterSink
import com.greensopinion.vectorvector.sink.TerrariumSink
import com.greensopinion.vectorvector.sink.VectorTileSink
import com.greensopinion.vectorvector.sink.contour.ContourOptions
import com.greensopinion.vectorvector.sink.hillshade.ResolutionPerPixel
import com.greensopinion.vectorvector.sink.vectorSchema
import com.greensopinion.vectorvector.util.closeSafely
import io.github.oshai.kotlinlogging.KotlinLogging
import picocli.CommandLine
import java.io.File
import java.time.Duration

private val log = KotlinLogging.logger { }

fun main(args: Array<String>) {
    val options = parseCommandLine(args)
    if (options.help) {
        CommandLine.usage(CliOptions(), System.out)
        return
    }
    val metricsProvider = SingletonMetricsProvider()
    val dataStore = cachingElevationDataStore(options, metricsProvider)

    val sinkToRepository = tileSinkToRepository(
        options,
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
    metricsProvider: SingletonMetricsProvider
): ElevationDataStore {
    val blockSize: Degrees
    val blockMapper: BlockOffsetMapper
    val store: BlockStore
    val cacheSize: Long
    if (options.dataFormat == DataSourceFormat.srtm) {
        blockSize = Degrees(5.0)
        store = SrtmBlockStore(
            folder = options.dataDir!!,
            metricsProvider = metricsProvider
        )
        blockMapper = SrtmBlockMapper(store.blockExtent, blockSize)
        cacheSize = 25
    } else if (options.dataFormat == DataSourceFormat.aw3d30) {
        blockSize = Degrees(1.0)
        store = Aw3d30ElevationDataStore(
            dataFolder = options.dataDir!!,
            outputFolder = options.dataDir!!,
            metricsProvider = metricsProvider
        )
        blockMapper = DegreeBlockMapper(store.blockExtent, blockSize)
        cacheSize = 200L
    } else {
        println("Using carto datastore")
        blockSize = Degrees(1.0)
        store = Carto30ElevationDataStore(
            dataFolder = options.dataDir!!,
            metricsProvider = metricsProvider
        )
        blockMapper = DegreeBlockMapper(store.blockExtent, blockSize)
        cacheSize = 200L
    }
    val tileExtent = 256
    return CachingElevationDataStore(
        BlockElevationDataStore(
            blockMapper = blockMapper,
            tileExtent = tileExtent,
            blockStore = CachingBlockStore(
                cacheSize,
                store,
                metricsProvider
            )
        )
    )
}

private fun tileSinkToRepository(
    options: CliOptions,
    dataStore: ElevationDataStore,
    metricsProvider: SingletonMetricsProvider,
    repositoryFactory: (name: String, format: String) -> TileRepository
): Map<TileSink, TileRepository> {
    val tileExtent = 256
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
    if (options.hillshadeRaster) {
        val repository = repositoryFactory("hillshade", "png")
        sinks[HillshadeRasterSink(
            extent = tileExtent,
            repository = repository,
            elevationDataStore = dataStore,
            resolutionPerPixel = ResolutionPerPixel(tileExtent = tileExtent, minZ = options.minZ, maxZ = options.maxZ),
            metricsProvider = metricsProvider
        )] = repository
    }
    if (options.vector) {
        val repository = repositoryFactory("vector", "pbf")
        val baseOptions = ContourOptions(minorLevel = 10, majorLevel = 50, epsilon = options.contourEpsilon)
        sinks[VectorTileSink(
            contourOptionsProvider = { tile ->
                if (tile.id.z < 9) {
                    baseOptions.copy(minorLevel = 500, majorLevel = 1000)
                } else if (tile.id.z <= 10) {
                    baseOptions.copy(minorLevel = 100, majorLevel = 200)
                } else if (tile.id.z < 12) {
                    baseOptions.copy(minorLevel = 20, majorLevel = 100)
                } else
                    baseOptions
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
                    name = "vectorvector $format tiles",
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
