package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Elevation
import com.greensopinion.vectorvector.ElevationTile
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.awt.image.Raster
import java.io.File
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import kotlin.math.max
import kotlin.math.roundToInt

class RasterTileReader(
    private val log: KLogger = KotlinLogging.logger {}
) {
    fun read(file: File): ElevationTile {
        log.info { "reading $file" }
        val readers = ImageIO.getImageReadersByFormatName("TIFF")
        require(readers.hasNext()) { "No TIFF readers!" }
        val reader = readers.next() as ImageReader
        ImageIO.createImageInputStream(file).use { stream ->
            reader.input = stream
            val raster = reader.readRaster(0, null)
            return RasterElevationTile(raster)
        }
    }
}

private class RasterElevationTile(val raster: Raster) : ElevationTile() {
    override val empty = false
    private val width = raster.width
    private val height = raster.height
    override val extent: Int = max(width, height)
    override fun get(x: Int, y: Int): Elevation {
        val xOffset =
            (if (width == extent) x else ((x.toDouble() / extent) * width).roundToInt()).coerceAtMost(width - 1)
        val yOffset =
            (if (height == extent) y else ((y.toDouble() / extent) * height).roundToInt()).coerceAtMost(height - 1)
        val sample = try {
            raster.getSample(xOffset, yOffset, 0)
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw Exception("invalid coordinates: ${xOffset},${yOffset} in [${raster.width},${raster.height}]")
        }
        if (sample == -32768 || sample == -9999) {
            return Elevation(meters = 0.0)
        }
        return Elevation(meters = sample.toDouble())
    }
}