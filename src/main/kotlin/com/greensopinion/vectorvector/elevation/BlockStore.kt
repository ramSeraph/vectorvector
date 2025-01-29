package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.ElevationTile

interface BlockStore {
    val blockExtent: Int
    fun provides(blockId: BlockId): Boolean
    fun loadAsync(area: List<BlockId>) {}
    fun load(blockId: BlockId) : ElevationTile
}