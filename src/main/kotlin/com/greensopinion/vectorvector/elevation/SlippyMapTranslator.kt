package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import com.greensopinion.vectorvector.TilePosition
import kotlin.math.atan
import kotlin.math.sinh

class SlippyMapTranslator(
    private val tileExtent: Int
) {
    fun map(position: TilePosition): Coordinates {
        val x = (position.tile.x * tileExtent) + position.position.x
        val y = (position.tile.y * tileExtent) + position.position.y
        val mapSize = tileExtent * (1 shl position.tile.z)
        val normalizedX = x.toDouble() / mapSize
        val normalizedY = y.toDouble() / mapSize

        return Coordinates(
            latitude = atan(sinh(Math.PI * (1 - 2 * normalizedY))) * (180.0 / Math.PI),
            longitude = normalizedX * 360.0 - 180.0,
        )
    }
}