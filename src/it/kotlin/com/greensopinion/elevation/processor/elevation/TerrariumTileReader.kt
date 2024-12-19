package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Elevation
import com.greensopinion.elevation.processor.ElevationTile
import java.io.File
import javax.imageio.ImageIO

class TerrariumTileReader {
    fun read(file: File): ElevationTile {
        val image = ImageIO.read(file)
        return object : ElevationTile {
            override val extent: Int = image.width
            override val empty = false

            override fun get(x: Int, y: Int): Elevation {
                require(x < image.width && y < image.height) { "$x,$y is not in ${image.width},${image.height}" }
                val rgb = image.getRGB(x, y)
                val r = ((rgb shr 16) and 0xFF)
                val g = ((rgb shr 8) and 0xFF)

                return Elevation(meters = ((r * 256) + g) - 32768)
            }
        }
    }
}