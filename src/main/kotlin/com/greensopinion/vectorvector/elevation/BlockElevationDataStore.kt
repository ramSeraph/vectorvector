package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Elevation
import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.Position
import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.TilePosition
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
        private val blockIdToBlockLocal = ThreadLocal<MutableMap<BlockId, ElevationTile>>()

        override fun get(x: Int, y: Int): Elevation {
            val coordinates = slippyMapTranslator.map(TilePosition(tile, Position(x, y)))
            val blockPosition = blockMapper.map(coordinates)
            val blockIdToBlock = blockIdToBlockLocal.get() ?: UnsafeCache().also { blockIdToBlockLocal.set(it) }
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

    private class UnsafeCache : LinkedHashMap<BlockId, ElevationTile>(101,0.4f,true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<BlockId, ElevationTile>?): Boolean {
            return size > maxCacheSize
        }
    }
}

private const val maxCacheSize = 50