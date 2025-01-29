package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import kotlin.math.floor

class DegreeBlockMapper(
    private val blockExtent: Int,
    private val blockSize: Degrees,
): BlockOffsetMapper {
    init {
        require(blockSize.degrees.toInt().toDouble() == blockSize.degrees)
    }

    val pixelSize = Degrees(blockSize.degrees / blockExtent)
    override fun map(coordinates: Coordinates): BlockOffset {
        val block = toBlockId(coordinates)
        val min = Coordinates(
            latitude = block.y.toDouble(),
            longitude = block.x.toDouble()
        )
        val max = Coordinates(
            latitude = min.latitude + blockSize.degrees,
            longitude = min.longitude + blockSize.degrees
        )
        val offset = Offset(
            x = (coordinates.longitude - min.longitude) / pixelSize.degrees,
            y = (max.latitude - coordinates.latitude) / pixelSize.degrees
        ).clamp(blockExtent.toDouble()-1)
        return BlockOffset(block,offset)
    }

    private fun toBlockId(coordinates: Coordinates) = BlockId(
        x = floor(coordinates.longitude / blockSize.degrees).toInt(),
        y = floor(coordinates.latitude / blockSize.degrees).toInt()
    )

    override fun mapArea(bottomLeft: Coordinates, topRight: Coordinates): List<BlockId> {
        val bottomLeftBlock = toBlockId(bottomLeft)
        val topRightBlock = toBlockId(topRight)
        val bottomLeftCoordinate = reverseMap(bottomLeftBlock)
        val topRightCoordinate = reverseMap(topRightBlock)
        val minLat = bottomLeftCoordinate.latitude.toInt()
        val maxLat = topRightCoordinate.latitude.toInt()
        val minLon = bottomLeftCoordinate.longitude.toInt()
        val maxLon = topRightCoordinate.longitude.toInt()
        val increment = blockSize.degrees.toInt()

        val area = mutableListOf<BlockId>()
        for (lat in minLat..maxLat step increment) {
            var lon = minLon
            do {
                area.add(map(Coordinates(latitude = lat.toDouble(), longitude = lon.toDouble())).blockId)
                lon = (lon + increment).clampLon()
            } while (lon != (maxLon + increment).clampLon())
        }
        return area
    }

    override fun reverseMap(blockId: BlockId): Coordinates = Coordinates(
        latitude = blockId.y.toDouble() * blockSize.degrees,
        longitude = blockId.x.toDouble() * blockSize.degrees
    )
}

private fun Int.clampLon() = (this + 180) % 360 - 180