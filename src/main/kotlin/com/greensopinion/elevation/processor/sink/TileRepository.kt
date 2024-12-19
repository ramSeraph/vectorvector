package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.TileId

interface TileRepository {
    fun store(tile: TileId,extension: String,bytes: ByteArray)
}