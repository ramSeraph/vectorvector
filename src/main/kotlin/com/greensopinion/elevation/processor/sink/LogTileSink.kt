package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.Tile
import com.greensopinion.elevation.processor.TileSink
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class LogTileSink(val log: KLogger = KotlinLogging.logger {}) : TileSink {
    override fun accept(tile: Tile) {
        println("Tile: ${tile.id}")
    }
}