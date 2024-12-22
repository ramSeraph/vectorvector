package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Elevation
import com.greensopinion.elevation.processor.ElevationTile

class ElevationInterpolator(val tile: ElevationTile) {

    fun get(position: Offset): Elevation {
        val topLeft = position.floor()
        val bottomRight = position.ceil()

        val topLeftElevation = tile.get(topLeft.x.toInt(), topLeft.y.toInt())
        val topRightElevation = tile.get(bottomRight.x.toInt(), topLeft.y.toInt())
        val bottomLeftElevation = tile.get(topLeft.x.toInt(), bottomRight.y.toInt())
        val bottomRightElevation = tile.get(bottomRight.x.toInt(), bottomRight.y.toInt())

        val distanceX = position.x % 1.0
        val distanceY = position.y % 1.0

        val eTopLeft = ((1.0 - distanceX)*(1.0-distanceY)) * topLeftElevation.meters
        val eTopRight = (distanceX*(1.0-distanceY))*topRightElevation.meters
        val eBottomLeft = ((1.0 - distanceX)*distanceY)*bottomLeftElevation.meters
        val eBottomRight = distanceX*distanceY*bottomRightElevation.meters

        return Elevation(meters = eTopLeft+eTopRight+eBottomRight+eBottomLeft)
    }
}
