package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.ElevationTile

interface BlockStore {
    fun load(blockId: BlockId) : ElevationTile
}