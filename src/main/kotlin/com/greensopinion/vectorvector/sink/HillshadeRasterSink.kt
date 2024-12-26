package com.greensopinion.vectorvector.sink

import com.greensopinion.vectorvector.Elevation
import com.greensopinion.vectorvector.Tile
import com.greensopinion.vectorvector.TileSink
import com.greensopinion.vectorvector.elevation.ElevationDataStore
import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.repository.TileRepository
import com.greensopinion.vectorvector.sink.hillshade.HornAlgorithm
import com.greensopinion.vectorvector.sink.hillshade.ResolutionPerPixel
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class HillshadeRasterSink(
    val extent: Int,
    val repository: TileRepository,
    val elevationDataStore: ElevationDataStore,
    val resolutionPerPixel: ResolutionPerPixel,
    private val metricsProvider: MetricsProvider
) : TileSink {
    private val initialSize = 1024 * 150

    override fun accept(tile: Tile): Boolean {
        val hornAlgorithm = HornAlgorithm(
            elevationDataStore,
            resolutionPerPixel
        )
        val hillshade = hornAlgorithm.create(tile.id)
        if (hillshade.empty) {
            return false
        }
        val image = BufferedImage(extent, extent, BufferedImage.TYPE_INT_ARGB)
        for (x in 0..<extent) {
            for (y in 0..<extent) {
                val illumination = hillshade.get(x, y)
                val v = illumination.meters.toInt() and 0xFF
                val argb = if (excludedRange(illumination)) 0 else ((v and 0xFF) shl 24) or
                        ((v and 0xFF) shl 16) or
                        ((v and 0xFF) shl 8) or
                        ((v and 0xFF) shl 0)
                image.setRGB(x, y, argb)
            }
        }
        val extension = "png"
        val output = ByteArrayOutputStream(initialSize)
        ImageIO.write(image, extension, output)
        repository.store(tile.id, extension, output.toByteArray())
        metricsProvider.get().addCount("HillshadeRasterTile")
        return true
    }

    private fun excludedRange(illumination: Elevation) = illumination > 130.0 && illumination < 220
}