package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.TileId

interface ElevationDataStore {
    fun get(tile: TileId): ElevationTile
}