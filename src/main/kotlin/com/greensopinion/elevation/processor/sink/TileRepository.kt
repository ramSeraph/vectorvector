package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.TileId
import java.io.Closeable

interface TileRepository : Closeable {
    fun store(tile: TileId,extension: String,bytes: ByteArray)
}