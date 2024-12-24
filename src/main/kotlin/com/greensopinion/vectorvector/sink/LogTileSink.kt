package com.greensopinion.vectorvector.sink

import com.greensopinion.vectorvector.Tile
import com.greensopinion.vectorvector.TileSink
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class LogTileSink(val log: KLogger = KotlinLogging.logger {}) : TileSink {
    override fun accept(tile: Tile) : Boolean {
        log.info { "Tile: ${tile.id}" }
        return true
    }
}