package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Elevation
import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.EmptyTile
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.ImageReader


class FilesystemBlockStore(
    private val blockExtent: Int,
    private val folder: File,
    private val log: KLogger = KotlinLogging.logger {}
) : BlockStore {
    override fun load(blockId: BlockId): ElevationTile {
        val file = File(folder, "srtm_${blockId.x.keyString()}_${blockId.y.keyString()}.tif")
        return if (file.exists()) load(file) else EmptyTile(blockExtent)
    }

    private fun load(file: File): ElevationTile {
        log.info { "reading $file" }
        val readers = ImageIO.getImageReadersByFormatName("TIFF")
        require(readers.hasNext()) { "No TIFF readers!" }
        val reader = readers.next() as ImageReader
        ImageIO.createImageInputStream(file).use { stream ->
            reader.input = stream
            val raster = reader.readRaster(0, null)
            return object : ElevationTile() {
                override val empty = false
                override val extent: Int
                    get() = raster.width
                override fun get(x: Int, y: Int): Elevation {
                    val sample = try {
                        raster.getSample(x, y, 0)
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        throw Exception("invalid coordinates: ${x},${y} in [${raster.width},${raster.height}]")
                    }
                    if (sample == -32768) {
                        return Elevation(meters = 0)
                    }
                    return Elevation(meters = sample)
                }
            }
        }
    }

    fun validateAll() {
        val files = folder.listFiles() ?: throw IllegalArgumentException("Data folder does not exist: $folder")
        val errors = mutableListOf<File>()
        for (file in files.filter { it.name.endsWith(".tif") }) {
            try {
                load(file)
            } catch (e: Exception) {
                errors.add(file)
                log.error(e) { "Cannot load file: $file" }
            }
        }
        if (errors.isNotEmpty()) {
            log.info { "The following ${errors.size} files could not be loaded: \n${errors.joinToString("\n")}" }
            throw Exception("${errors.size} files had errors")
        }
    }
}

private fun Int.keyString() = this.toString().padStart(2, '0')