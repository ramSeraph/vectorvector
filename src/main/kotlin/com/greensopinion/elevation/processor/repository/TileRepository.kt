package com.greensopinion.elevation.processor.repository

import com.greensopinion.elevation.processor.TileId
import java.io.Closeable

interface TileRepository : Closeable {
    fun store(tile: TileId,extension: String,bytes: ByteArray)
}