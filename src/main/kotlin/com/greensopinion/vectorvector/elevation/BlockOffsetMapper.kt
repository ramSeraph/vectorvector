package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates

interface BlockOffsetMapper {
    fun mapArea(bottomLeft: Coordinates, topRight: Coordinates): List<BlockId>
    fun map(coordinates: Coordinates) : BlockOffset
    fun reverseMap(blockId: BlockId): Coordinates
}