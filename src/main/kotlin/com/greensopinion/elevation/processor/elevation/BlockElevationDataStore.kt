package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Elevation
import com.greensopinion.elevation.processor.ElevationTile
import com.greensopinion.elevation.processor.Position
import com.greensopinion.elevation.processor.TileId
import com.greensopinion.elevation.processor.TilePosition
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class BlockElevationDataStore(
    blockExtent: Int,
    blockSize: Degrees,
    private val tileExtent: Int,
    private val blockStore: BlockStore,
    private val log: KLogger = KotlinLogging.logger {}
) : ElevationDataStore {
    private val slippyMapTranslator = SlippyMapTranslator(tileExtent)
    private val blockMapper = BlockMapper(blockExtent, blockSize)

    override fun get(tile: TileId): ElevationTile = object : ElevationTile() {
        override val extent = tileExtent
        override val empty by lazy {
            isEmpty(tile, Position(0, 0)) &&
                    isEmpty(tile, Position(tileExtent - 1, tileExtent - 1)) &&
                    isEmpty(tile, Position(0, tileExtent - 1)) &&
                    isEmpty(tile, Position(tileExtent - 1, 0))
        }
        private val blockIdToBlock = mutableMapOf<BlockId, ElevationTile>()

        override fun get(x: Int, y: Int): Elevation {
            val coordinates = slippyMapTranslator.map(TilePosition(tile, Position(x, y)))
            val blockPosition = blockMapper.map(coordinates)
            val block = blockIdToBlock[blockPosition.blockId]
                ?: blockIdToBlock.computeIfAbsent(blockPosition.blockId) { blockStore.load(blockPosition.blockId) }
            return ElevationInterpolator(block).get(blockPosition.position)
        }

        private fun isEmpty(tile: TileId, position: Position): Boolean {
            val coordinates = slippyMapTranslator.map(TilePosition(tile, position))
            val blockPosition = blockMapper.map(coordinates)
            val block = blockStore.load(blockPosition.blockId)
            return block.empty
        }
    }
}