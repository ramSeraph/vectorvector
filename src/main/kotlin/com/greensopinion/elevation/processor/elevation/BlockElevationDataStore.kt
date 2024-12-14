package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.*
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.roundToInt

class BlockElevationDataStore(
    private val blockExtent: Int,
    private val blockSize: Degrees,
    private val tileExtent: Int,
    private val blockStore: BlockStore,
    private val log: KLogger = KotlinLogging.logger {}
) : ElevationDataStore {
    private val slippyMapTranslator = SlippyMapTranslator(tileExtent)
    private val blockMapper = BlockMapper(blockExtent, blockSize)

    override fun get(tile: TileId): ElevationTile = object : ElevationTile {
        override fun get(x: Int, y: Int): Elevation {
            val coordinates = slippyMapTranslator.map(TilePosition(tile, Position(x, y)))
            if (x == 255 && y == 255) {
                log.info {
                    val c = slippyMapTranslator.map(TilePosition(tile, Position(x, y)))
                    "tile ${tile} is $c";
                }
            }
            val blockPosition = blockMapper.map(coordinates)
            val block = blockStore.load(blockPosition.blockId)
            return block.get(blockPosition.position.x.roundToInt(),blockPosition.position.y.roundToInt())
        }
    }
}