package com.greensopinion.vectorvector.sink.hillshade

import com.greensopinion.vectorvector.Elevation
import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.ElevationTileArea
import com.greensopinion.vectorvector.EmptyTile
import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.elevation.ElevationDataStore
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

private typealias Radians = Double
typealias Degrees = Double

class HornAlgorithm(
    private val dataStore: ElevationDataStore,
    private val resolutionPerPixel: ResolutionPerPixel,
    private val sunAzimuth: Degrees = 315.0,
    private val sunAltitude: Degrees = 45.0,
) {

    fun create(tileId: TileId): ElevationTile {
        val tile = dataStore.get(tileId)
        if (tile.empty) {
            return EmptyTile(tile.extent)
        }
        val elevations = ElevationTileArea(tileId, tile, dataStore).materialize(buffer = 1)
        val resolution = resolutionPerPixel.metersPerPixel(tileId)
        val zenithAngle = 90 - sunAltitude
        val azimuthRad = Math.toRadians(360 - sunAzimuth + 90)
        val zenithRad = Math.toRadians(zenithAngle)
        return object : ElevationTile() {
            override val empty = false
            override val extent = tile.extent

            override fun get(x: Int, y: Int): Elevation {
                val slopeAspect = elevations.slopeAspect(resolution, x, y)
                val illumination = cos(zenithRad) * cos(slopeAspect.slope) +
                        sin(zenithRad) * sin(slopeAspect.slope) * cos(azimuthRad - slopeAspect.aspect)
                return Elevation(meters = (255 * max(0.0, illumination)))
            }
        }.materialize()
    }
}

private class SlopeAspect(val slope: Radians, val aspect: Radians)


private fun ElevationTile.slopeAspect(resolution: Double, x: Int, y: Int): SlopeAspect {
    var dzdx = derivativeX(resolution, x, y)
    var dzdy = derivativeY(resolution, x, y)
    if (dzdx == Double.NEGATIVE_INFINITY || dzdy == Double.NEGATIVE_INFINITY) {
        // assume flat
        dzdx = 0.0
        dzdy = 0.0
    }

    val slope = atan(sqrt(dzdx * dzdx + dzdy * dzdy))
    val aspect = atan2(dzdy, -dzdx).let {
        // in range [0, 2π]
        if (it < 0) it + 2 * Math.PI else it
    }
    return SlopeAspect(slope, aspect)
}

private fun ElevationTile.derivativeX(resolution: Double, x: Int, y: Int): Double {
    val left = get(x - 1, y)
    val right = get(x + 1, y)
    return if (left.valid && right.valid) {
        (right.meters - left.meters) / (2 * resolution)
    } else Double.NEGATIVE_INFINITY
}

private fun ElevationTile.derivativeY(resolution: Double, x: Int, y: Int): Double {
    val up = get(x, y - 1)
    val down = get(x, y + 1)
    return if (up.valid && down.valid) {
        (down.meters - up.meters) / (2 * resolution)
    } else Double.NEGATIVE_INFINITY
}