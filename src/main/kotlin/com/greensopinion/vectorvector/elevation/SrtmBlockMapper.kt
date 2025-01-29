package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import kotlin.math.floor


class SrtmBlockMapper(
    private val blockExtent: Int,
    private val blockSize: Degrees,
) : BlockOffsetMapper {
    val pixelSize = Degrees(blockSize.degrees / blockExtent)
    private val xOffset = 1
    private val yOffset = 5

    override fun map(coordinates: Coordinates): BlockOffset {
        val block = BlockId(
            x = floor((coordinates.longitude + 180.0) / blockSize.degrees).toInt() + xOffset,
            y = floor((90.0 - coordinates.latitude) / blockSize.degrees).toInt() - yOffset
        )
        val min = Coordinates(
            latitude = -((block.y + yOffset.toDouble()) * blockSize.degrees - 90.0) - blockSize.degrees,
            longitude = (block.x - xOffset.toDouble()) * blockSize.degrees - 180.0
        )
        val max = Coordinates(
            latitude = min.latitude + blockSize.degrees,
            longitude = min.longitude + blockSize.degrees
        )
        val offset = Offset(
            x = (coordinates.longitude - min.longitude) / pixelSize.degrees,
            y = (max.latitude - coordinates.latitude) / pixelSize.degrees
        ).clamp(blockExtent.toDouble()-1)
        return BlockOffset(block, offset)
    }

    /**
     * maps a block ID to the bottom left coordinates of the corresponding area
     */
    override fun reverseMap(blockId: BlockId): Coordinates = Coordinates(
        latitude = 90.0 - ((blockId.y + yOffset) * blockSize.degrees),
        longitude = (blockId.x - xOffset) * blockSize.degrees - 180.0,
    )

    override fun mapArea(bottomLeft: Coordinates, topRight: Coordinates): List<BlockId> {
        val degreeBlockMapper = DegreeBlockMapper(blockExtent, blockSize)
        val degreeBlocks = degreeBlockMapper.mapArea(bottomLeft, topRight)
        return degreeBlocks.map { degreeBlockMapper.reverseMap(it) }.map { map(it).blockId }.toList()
    }

}