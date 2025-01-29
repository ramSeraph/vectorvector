package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Elevation
import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.Position
import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.TilePosition
import com.greensopinion.vectorvector.util.UnsafeCache
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

class BlockElevationDataStore(
    private val blockMapper: BlockOffsetMapper,
    private val tileExtent: Int,
    private val blockStore: BlockStore,
    private val log: KLogger = KotlinLogging.logger {}
) : ElevationDataStore {
    private val slippyMapTranslator = SlippyMapTranslator(tileExtent)

    override fun get(tile: TileId): ElevationTile = object : ElevationTile() {
        override val extent = tileExtent
        override val empty by lazy { isEmpty(tile) }
        private val blockIdToBlockLocal = ThreadLocal<MutableMap<BlockId, ElevationTile>>()

        override fun get(x: Int, y: Int): Elevation {
            val coordinates = slippyMapTranslator.map(TilePosition(tile, Position(x, y)))
            val blockPosition = blockMapper.map(coordinates)
            val blockIdToBlock = blockIdToBlockLocal.get()
                ?: UnsafeCache<BlockId, ElevationTile>(maxCacheSize).also { blockIdToBlockLocal.set(it) }
            val block = blockIdToBlock[blockPosition.blockId]
                ?: blockIdToBlock.computeIfAbsent(blockPosition.blockId) { blockStore.load(blockPosition.blockId) }
            return ElevationInterpolator(block).get(blockPosition.position)
        }

        private fun isEmpty(tile: TileId): Boolean {
            val bottomLeft = slippyMapTranslator.map(TilePosition(tile, Position(0, extent - 1)))
            val topRight = slippyMapTranslator.map(TilePosition(tile, Position(extent - 1, 0)))
            val blocks = blockMapper.mapArea(bottomLeft, topRight)
            if (blocks.size > 4) {
                blockStore.loadAsync(blocks)
            }
            val nonEmpty = blocks.any { blockStore.provides(it) }
            return !nonEmpty
        }
    }
}

private const val maxCacheSize = 3