package com.greensopinion.elevation.processor

@JvmInline
value class Elevation(val meters: Double)

val INVALID_ELEVATION = Elevation(meters = Double.NEGATIVE_INFINITY)

interface ElevationTile {
    val empty: Boolean
    fun get(x: Int, y: Int): Elevation
}

object EmptyTile : ElevationTile {
    override val empty = true
    override fun get(x: Int, y: Int): Elevation = INVALID_ELEVATION
}