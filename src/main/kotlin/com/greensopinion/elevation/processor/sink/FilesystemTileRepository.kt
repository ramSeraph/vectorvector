package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.TileId
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