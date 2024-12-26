package com.greensopinion.vectorvector.sink.hillshade

import com.greensopinion.vectorvector.Position
import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.TilePosition
import com.greensopinion.vectorvector.elevation.SlippyMapTranslator
import kotlin.math.PI
import kotlin.math.cos

class ResolutionPerPixel(
    private val tileExtent: Int,
    private val minZ: Int,
    private val maxZ: Int
) {
    private val keyToResolution = createKeyToResolution()

    private fun createKeyToResolution(): Map<Key, Double> {
        val keyToResolution = mutableMapOf<Key, Double>()
        val translator = SlippyMapTranslator(tileExtent)
        (minZ..maxZ).forEach { z ->
            val yDimension = 1 shl z
            (0..<yDimension).forEach { y ->
                val coordinates = translator.map(TilePosition(TileId(z, 0, y), Position(0, tileExtent / 2)))
                keyToResolution[Key(z, y)] = 156543.03 * cos(coordinates.latitude.toRadians()) / (1 shl z)
            }
        }
        return keyToResolution.toMap()
    }

    fun metersPerPixel(tile: TileId) : Double = keyToResolution[Key(tile.z, tile.y)] ?: throw IllegalArgumentException(tile.toString())
}

private data class Key(val z: Int, val y: Int)

private fun Double.toRadians() = this * PI / 180