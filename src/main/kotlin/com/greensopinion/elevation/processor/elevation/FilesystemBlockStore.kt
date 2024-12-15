package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Elevation
import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.EmptyTile
import com.greensopinion.elevation.processor.INVALID_ELEVATION
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.ImageReader


class FilesystemBlockStore(
    private val folder: File,
    private val log: KLogger = KotlinLogging.logger {}
) : BlockStore {
    override fun load(blockId: BlockId): ElevationTile {
        val file = File(folder, "srtm_${blockId.x.keyString()}_${blockId.y.keyString()}.tif")
        return if (file.exists()) load(file) else EmptyTile
    }

    private fun load(file: File): ElevationTile {
        log.info { "reading $file" }
        val readers = ImageIO.getImageReadersByFormatName("TIFF")
        require(readers.hasNext()) { "No TIFF readers!" }
        val reader = readers.next() as ImageReader
        ImageIO.createImageInputStream(file).use { stream ->
            reader.input = stream
            val raster = reader.readRaster(0, null)
            return object : ElevationTile {
                override val empty = false
                override fun get(x: Int, y: Int): Elevation {
                    val sample = try {
                        raster.getSample(x, y, 0)
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        throw Exception("invalid coordinates: ${x},${y} in [${raster.width},${raster.height}]")
                    }
                    if (sample == -32768) {
                        return Elevation(meters = 0.0)
                    }
                    return Elevation(meters = sample.toDouble())
                }
            }
        }
    }
}

private fun Int.keyString() = this.toString().padStart(2, '0')