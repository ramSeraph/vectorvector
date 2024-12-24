package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.ElevationTile

interface BlockStore {
    fun load(blockId: BlockId) : ElevationTile
}