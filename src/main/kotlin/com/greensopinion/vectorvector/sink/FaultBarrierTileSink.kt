package com.greensopinion.vectorvector.sink

import com.greensopinion.vectorvector.Tile
import com.greensopinion.vectorvector.TileSink
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class FaultBarrierTileSink(
    private val delegate: TileSink,
    private val log: KLogger = KotlinLogging.logger { }
) : TileSink {
    override fun accept(tile: Tile): Boolean = try {
        delegate.accept(tile)
    } catch (e: Exception) {
        log.error(e) { "Exception on tile ${tile.id}" }
        false
    }
}