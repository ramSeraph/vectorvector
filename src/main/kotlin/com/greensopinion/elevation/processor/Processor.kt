package com.greensopinion.elevation.processor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class Processor(
    private val tileRange: TileRange,
    private val sink: TileSink
) {

    fun process() = runBlocking {
        tileRange.tiles().asFlow().map { tileId ->
            async {
                Tile(tileId)
            }
        }.map { it.await() }.collect {
            sink.accept(it)
        }
    }
}