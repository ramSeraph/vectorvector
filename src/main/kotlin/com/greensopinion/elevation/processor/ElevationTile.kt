package com.greensopinion.elevation.processor

@JvmInline
value class Elevation(val meters: Double) {
    val valid: Boolean get() = meters > INVALID_ELEVATION.meters
    operator fun times(multiplier: Double) = Elevation(meters = meters * multiplier)
    operator fun minus(other: Elevation): Elevation =
        if (other.valid && valid) Elevation(meters = meters - other.meters) else this

    operator fun compareTo(other: Elevation): Int {
        return this.meters.compareTo(other.meters)
    }

    operator fun compareTo(other: Int): Int {
        return this.meters.compareTo(other)
    }

    override fun toString(): String = "$meters"
}

data class ElevationBounds(val min: Elevation, val max: Elevation)

val INVALID_ELEVATION = Elevation(meters = Double.NEGATIVE_INFINITY)

abstract class ElevationTile {
    abstract val empty: Boolean
    abstract val extent: Int
    val elevationBounds: ElevationBounds by lazy {
        var bounds = ElevationBounds(min = INVALID_ELEVATION, max = INVALID_ELEVATION)
        for (x in 0..<extent) {
            for (y in 0..<extent) {
                val elevation = get(x, y)
                if (elevation.valid) {
                    if (!bounds.min.valid || bounds.min > elevation) {
                        bounds = bounds.copy(min = elevation)
                    }
                    if (!bounds.max.valid || bounds.max < elevation) {
                        bounds = bounds.copy(max = elevation)
                    }
                }
            }
        }
        bounds
    }

    abstract fun get(x: Int, y: Int): Elevation

    open fun materialize(): ElevationTile = if (empty) this else MaterializedTile(this)

    fun scale(multiplier: Double): ElevationTile =
        if (multiplier == 1.0 || empty) this else ScaledTile(multiplier, this)
}

class EmptyTile(
    override val extent: Int
) : ElevationTile() {
    override val empty = true
    override fun get(x: Int, y: Int): Elevation = INVALID_ELEVATION
}

private class ScaledTile(
    private val multiplier: Double,
    private val delegate: ElevationTile
) : ElevationTile() {
    override val empty = delegate.empty
    override val extent = delegate.extent

    override fun get(x: Int, y: Int): Elevation {
        val elevation = delegate.get(x, y)
        return if (elevation == INVALID_ELEVATION) INVALID_ELEVATION else elevation * multiplier
    }
}

private class MaterializedTile(delegate: ElevationTile) : ElevationTile() {
    override val empty = delegate.empty
    override val extent = delegate.extent
    private val data = DoubleArray(extent * extent)

    init {
        for (x in 0..<extent) {
            for (y in 0..<extent) {
                data[x + y * extent] = delegate.get(x, y).meters
            }
        }
    }

    override fun get(x: Int, y: Int): Elevation = Elevation(meters = data[x + y * extent])

    override fun materialize(): ElevationTile = this
}

