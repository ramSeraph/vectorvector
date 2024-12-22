package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.Tile
import com.greensopinion.elevation.processor.TileSink
import com.greensopinion.elevation.processor.elevation.ElevationDataStore
import com.greensopinion.elevation.processor.metrics.MetricsProvider
import com.greensopinion.elevation.processor.sink.contour.ContourLineGenerator
import com.greensopinion.elevation.processor.sink.contour.ContourOptions
import com.greensopinion.elevation.processor.sink.contour.ContourVectorTileMapper
import vector_tile.VectorTile

class VectorTileSink(
    private val contourOptions: ContourOptions,
    private val repository: TileRepository,
    private val elevationDataStore: ElevationDataStore,
    private val metricsProvider: MetricsProvider
) : TileSink {
    override fun accept(tile: Tile): Boolean {
        val elevationTile = elevationDataStore.get(tile.id)
        if (elevationTile.empty) {
            return false
        }
        val linesByElevation = ContourLineGenerator(contourOptions, elevationDataStore).generate(tile.id)
        if (linesByElevation.isEmpty()) {
            return false
        }
        val vectorTile = VectorTile.Tile.newBuilder().addLayers(
            ContourVectorTileMapper(contourOptions,linesByElevation).apply()
        ).build()
        repository.store(tile.id, "pbf", vectorTile.toByteArray())
        metricsProvider.get().addCount("VectorTile")
        return true
    }
}
