package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.Tile
import com.greensopinion.elevation.processor.TileSink

class CompositeTileSink(
    private val delegates: List<TileSink>
) : TileSink {
    override fun accept(tile: Tile): Boolean {
        val results = delegates.map { it.accept(tile) }.toList()
        return results.any { it }
    }
}