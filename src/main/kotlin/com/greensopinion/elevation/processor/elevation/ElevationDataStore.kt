package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.TileId

interface ElevationDataStore {
    fun get(tile: TileId): ElevationTile
}