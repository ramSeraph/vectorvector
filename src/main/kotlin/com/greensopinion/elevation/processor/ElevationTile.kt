package com.greensopinion.elevation.processor

@JvmInline
value class Elevation(val meters: Double)

val INVALID_ELEVATION = Elevation(meters = Double.NEGATIVE_INFINITY)

interface ElevationTile {
    fun get(x: Int, y: Int): Elevation
}

object EmptyTile : ElevationTile {
    override fun get(x: Int, y: Int): Elevation = INVALID_ELEVATION
}