package com.greensopinion.vectorvector.sink

import com.greensopinion.vectorvector.Tile
import com.greensopinion.vectorvector.TileSink

class CompositeTileSink(
    private val delegates: List<TileSink>
) : TileSink {
    override fun accept(tile: Tile): Boolean {
        val results = delegates.map { it.accept(tile) }.toList()
        return results.any { it }
    }
}