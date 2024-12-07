package com.greensopinion.elevation.processor

data class TileId(val z: Int, val x: Int, val y: Int)

class Tile(
    val id: TileId
) {
}