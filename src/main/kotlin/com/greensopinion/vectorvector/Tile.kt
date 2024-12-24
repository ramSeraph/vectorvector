package com.greensopinion.vectorvector

data class TileId(val z: Int, val x: Int, val y: Int)
data class Position(val x: Int,val y: Int)

class TilePosition(
    val tile: TileId,
    val position: Position
)

class Tile(
    val id: TileId
)