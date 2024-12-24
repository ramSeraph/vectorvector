package com.greensopinion.vectorvector.repository

import com.greensopinion.vectorvector.TileId
import java.io.Closeable

interface TileRepository : Closeable {
    fun store(tile: TileId,extension: String,bytes: ByteArray)
}