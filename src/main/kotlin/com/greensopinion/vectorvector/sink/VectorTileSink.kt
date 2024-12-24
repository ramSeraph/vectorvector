package com.greensopinion.vectorvector.sink

import com.greensopinion.vectorvector.Tile
import com.greensopinion.vectorvector.TileSink
import com.greensopinion.vectorvector.elevation.ElevationDataStore
import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.repository.TileRepository
import com.greensopinion.vectorvector.sink.contour.ContourLineGenerator
import com.greensopinion.vectorvector.sink.contour.ContourOptions
import com.greensopinion.vectorvector.sink.contour.ContourVectorTileMapper
import vector_tile.VectorTile

class VectorTileSink(
    private val contourOptionsProvider: (tile: Tile) -> ContourOptions,
    private val repository: TileRepository,
    private val elevationDataStore: ElevationDataStore,
    private val metricsProvider: MetricsProvider
) : TileSink {
    override fun accept(tile: Tile): Boolean {
        val elevationTile = elevationDataStore.get(tile.id)
        if (elevationTile.empty) {
            return false
        }
        val contourOptions = contourOptionsProvider(tile)
        val linesByElevation = ContourLineGenerator(contourOptions, elevationDataStore).generate(tile.id)
        if (linesByElevation.isEmpty()) {
            return false
        }
        val vectorTile = VectorTile.Tile.newBuilder().addLayers(
            ContourVectorTileMapper(contourOptions, linesByElevation).apply()
        ).build()
        repository.store(tile.id, "pbf", vectorTile.toByteArray())
        metricsProvider.get().addCount("VectorTile")
        return true
    }
}
