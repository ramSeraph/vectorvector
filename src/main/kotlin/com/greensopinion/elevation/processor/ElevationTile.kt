package com.greensopinion.elevation.processor

@JvmInline
value class Elevation(val meters: Int)

val INVALID_ELEVATION = Elevation(meters = Int.MIN_VALUE)

interface ElevationTile {
    val empty: Boolean
    val extent: Int
    fun get(x: Int, y: Int): Elevation

    fun materialize(): ElevationTile = if (empty) this else MaterializedTile(this)
}

class EmptyTile(
    override val extent: Int
) : ElevationTile {
    override val empty = true
    override fun get(x: Int, y: Int): Elevation = INVALID_ELEVATION
}

private class MaterializedTile(delegate: ElevationTile) : ElevationTile {
    override val empty = delegate.empty
    override val extent = delegate.extent
    private val data = IntArray(extent*extent)
    init {
        for (x in 0..<extent) {
            for (y in 0..<extent) {
                data[x+y*extent] = delegate.get(x,y).meters
            }
        }
    }
    override fun get(x: Int, y: Int): Elevation = Elevation(meters = data[x+y*extent])

    override fun materialize(): ElevationTile = this
}

