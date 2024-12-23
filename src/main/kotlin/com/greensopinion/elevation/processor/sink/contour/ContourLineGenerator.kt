package com.greensopinion.elevation.processor.sink.contour

import com.greensopinion.elevation.processor.Elevation
import com.greensopinion.elevation.processor.ElevationTileArea
import com.greensopinion.elevation.processor.TileId
import com.greensopinion.elevation.processor.elevation.ElevationDataStore
import kotlin.math.roundToInt

data class DoublePoint(val x: Double, val y: Double) {
    operator fun times(multiplier: Double): DoublePoint = DoublePoint(x * multiplier, y * multiplier)
    operator fun div(divisor: Double): DoublePoint = DoublePoint(x / divisor, y / divisor)
    operator fun plus(other: DoublePoint): DoublePoint = DoublePoint(x + other.x, y + other.y)
    fun round() = DoublePoint(x.roundToInt().toDouble(),y.roundToInt().toDouble())
}
class Line(val points: List<DoublePoint>)

class ContourLineGenerator(
    private val options: ContourOptions,
    private val dataStore: ElevationDataStore
) {
    fun generate(tile: TileId): Map<Elevation, List<Line>> {
        val area = ElevationTileArea(tile, dataStore.get(tile), dataStore).scale(options.multiplier).materialize(buffer = options.buffer)
        if (area.empty) {
            return mapOf()
        }
        val algorithm = MarchingSquares(area,options.extent,options.buffer)
        val isolines = algorithm.generateIsolines(options.minorLevel)
        return isolines.entries.associate { Pair(Elevation(meters = it.key.toDouble()), it.value.toLines()) }
    }
}

private fun List<List<DoublePoint>>.toLines() : List<Line> = this.map { it.toLine() }

private fun List<DoublePoint>.toLine() : Line = Line(this)