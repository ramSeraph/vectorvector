package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Coordinates
import kotlin.math.floor


class BlockMapper(
    private val blockExtent: Int,
    private val blockSize: Degrees,
) {
    val pixelSize = Degrees(blockSize.degrees / blockExtent)
    private val xOffset = 1
    private val yOffset = 5

    fun map(coordinates: Coordinates): BlockOffset {
        val block = BlockId(
            x = floor((coordinates.longitude + 180.0) / blockSize.degrees).toInt() + xOffset,
            y = floor((90.0 - coordinates.latitude) / blockSize.degrees).toInt() - yOffset
        )
        val min = Coordinates(
            latitude = -((block.y + yOffset) * blockSize.degrees - 90.0) - blockSize.degrees,
            longitude = (block.x - xOffset.toDouble()) * blockSize.degrees - 180.0
        )
        val max = Coordinates(
            latitude = min.latitude + blockSize.degrees,
            longitude = min.longitude + blockSize.degrees
        )
        val offset = Offset(
            x = (coordinates.longitude - min.longitude) / pixelSize.degrees,
            y = (coordinates.latitude - min.latitude) / pixelSize.degrees
        )
        return BlockOffset(block, offset)
    }
}