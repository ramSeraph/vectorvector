package com.greensopinion.vectorvector.sink

import com.greensopinion.vectorvector.Elevation
import com.greensopinion.vectorvector.INVALID_ELEVATION
import com.greensopinion.vectorvector.Tile
import com.greensopinion.vectorvector.TileSink
import com.greensopinion.vectorvector.elevation.ElevationDataStore
import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.repository.TileRepository
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class TerrariumSink(
    val extent: Int,
    val repository: TileRepository,
    val elevationDataStore: ElevationDataStore,
    private val metricsProvider: MetricsProvider
) : TileSink {
    private val initialSize = 1024*150

    override fun accept(tile: Tile) :Boolean{
        val elevationTile = elevationDataStore.get(tile.id)
        if (elevationTile.empty) {
            return false
        }
        val image = BufferedImage(extent, extent, BufferedImage.TYPE_INT_RGB)
        var nonZero = false
        for (x in 0..<extent) {
            for (y in 0..<extent) {
                val elevation = elevationTile.get(x, y)
                nonZero = nonZero || elevation.meters > 0.0
                image.setRGB(x, y, encodeTerrarium(elevation))
            }
        }
        if (!nonZero) {
            return false
        }
        val extension = "png"
        val output = ByteArrayOutputStream(initialSize)
        ImageIO.write(image,extension,output)
        repository.store(tile.id,extension,output.toByteArray())
        metricsProvider.get().addCount("TerrariumTile")

        return true
    }

    private fun encodeTerrarium(elevation: Elevation): Int {
        var meters = elevation.meters
        if (elevation == INVALID_ELEVATION) {
            meters = 0.0
        }
        val shiftedElevation = (meters + 32768.0)
        val red = (shiftedElevation / 256).toInt()
        val green = (shiftedElevation % 256).toInt()
        val blue = ((shiftedElevation - shiftedElevation.toInt()) * 256).toInt()

        return ((red and 0xFF) shl 16) or
                ((green and 0xFF) shl 8) or
                (blue and 0xFF)
    }
}