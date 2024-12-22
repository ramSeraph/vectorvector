package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.elevation.ElevationDataStore

/**
 * A tile that provides access outside of its extent.
 */
class ElevationTileArea(
    private val tileId: TileId,
    private val tile: ElevationTile,
    private val store: ElevationDataStore
) : ElevationTile() {

    override val empty = tile.empty
    override val extent = tile.extent

    private val top: ElevationTile by lazy {
        store.get(tileId.copy(y = tileId.y - 1))
    }
    private val topLeft: ElevationTile by lazy {
        store.get(tileId.copy(x = tileId.x - 1, y = tileId.y - 1))
    }
    private val topRight: ElevationTile by lazy {
        store.get(tileId.copy(x = tileId.x + 1, y = tileId.y - 1))
    }
    private val left: ElevationTile by lazy {
        store.get(tileId.copy(x = tileId.x - 1))
    }
    private val right: ElevationTile by lazy {
        store.get(tileId.copy(x = tileId.x + 1))
    }
    private val bottom: ElevationTile by lazy {
        store.get(tileId.copy(y = tileId.y + 1))
    }
    private val bottomLeft: ElevationTile by lazy {
        store.get(tileId.copy(x = tileId.x - 1, y = tileId.y + 1))
    }
    private val bottomRight: ElevationTile by lazy {
        store.get(tileId.copy(x = tileId.x + 1, y = tileId.y + 1))
    }
    override fun get(x: Int, y: Int): Elevation {
        var xOffset = 0
        var yOffset = 0
        var delegate = tile;
        if (x < 0) {
            xOffset = tile.extent
            if (y < 0) {
                delegate = topLeft
                yOffset = tile.extent
            } else if (y >= tile.extent) {
                delegate = bottomLeft
                yOffset = -tile.extent
            } else {
                delegate = left
            }
        } else if (x >= tile.extent) {
            xOffset = -tile.extent
            if (y < 0) {
                delegate = topRight
                yOffset = tile.extent
            } else if (y >= tile.extent) {
                delegate = bottomRight
                yOffset = -tile.extent
            } else {
                delegate = right
            }
        } else if (y < 0) {
            yOffset = tile.extent
            delegate = top
        } else if (y >= tile.extent) {
            yOffset = -tile.extent
            delegate = bottom
        }
        return delegate.get(x + xOffset, y + yOffset)
    }
}