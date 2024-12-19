package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.Tile
import com.greensopinion.elevation.processor.TileSink
import com.greensopinion.elevation.processor.elevation.ElevationDataStore
import com.greensopinion.elevation.processor.metrics.MetricsProvider

class VectorTileSink(
    private val repository: TileRepository,
    private val elevationDataStore: ElevationDataStore,
    private val metricsProvider: MetricsProvider
) : TileSink {
    override fun accept(tile: Tile): Boolean {
        val elevationTile = elevationDataStore.get(tile.id)
        if (elevationTile.empty) {
            return false
        }
        val bytes = vector_tile.tile {

        }.toByteArray()
        repository.store(tile.id, "pbf", bytes)
        metricsProvider.get().addCount("VectorTile")
        return true
    }
}