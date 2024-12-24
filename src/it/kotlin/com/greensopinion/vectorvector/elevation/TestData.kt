package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.metrics.SingletonMetricsProvider
import java.io.File

private val metricsProvider = SingletonMetricsProvider()
val testBlockStore = CachingBlockStore(
    FilesystemBlockStore(
        blockExtent = 6000,
        folder = File("../../data/tif"),
        metricsProvider
    ),
    metricsProvider
)

fun referenceTerrariumTile(tileId: TileId): ElevationTile =
    TerrariumTileReader().read(File("src/it/resources/terrarium_${tileId.z}_${tileId.x}_${tileId.y}.png"))
