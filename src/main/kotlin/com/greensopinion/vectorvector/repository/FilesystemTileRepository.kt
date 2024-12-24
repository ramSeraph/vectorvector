package com.greensopinion.vectorvector.repository

import com.greensopinion.vectorvector.TileId
import java.io.File

class FilesystemTileRepository(
    private val outputFolder: File
) : TileRepository {
    override fun store(tile: TileId, extension: String, bytes: ByteArray) {
        val file = File(outputFolder, "${tile.z}/${tile.x}/${tile.y}.$extension")
        file.parentFile.mkdirs()
        file.writeBytes(bytes)
    }

    override fun close() {
        // nothing to do!
    }
}