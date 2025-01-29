package com.greensopinion.vectorvector.sink

import com.greensopinion.vectorvector.Tile
import com.greensopinion.vectorvector.TileSink
import com.greensopinion.vectorvector.elevation.ElevationDataStore
import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.repository.TileRepository
import com.greensopinion.vectorvector.sink.contour.ContourLineGenerator
import com.greensopinion.vectorvector.sink.contour.ContourOptions
import com.greensopinion.vectorvector.sink.contour.ContourVectorTileMapper
import com.greensopinion.vectorvector.sink.contour.LineSimplifier
import vector_tile.VectorTile

class VectorTileSink(
    private val contourOptionsProvider: (tile: Tile) -> ContourOptions,
    private val repository: TileRepository,
    private val elevationDataStore: ElevationDataStore,
    private val metricsProvider: MetricsProvider
) : TileSink {
    override fun accept(tile: Tile): Boolean {
        val elevationTile = elevationDataStore.get(tile.id)
        if (elevationTile.empty) {
            return false
        }
        val contourOptions = contourOptionsProvider(tile)
        var linesByElevation = ContourLineGenerator(contourOptions, elevationDataStore).generate(tile.id)
        if (linesByElevation.isEmpty() || linesByElevation.all { it.value.isEmpty() }) {
            return false
        }
        val epsilon = contourOptions.epsilon?.toDouble()
        if (epsilon != null && epsilon > 0.0) {
            linesByElevation = linesByElevation.map {
                Pair(it.key, it.value.map { line -> LineSimplifier(epsilon).simplify(line) })
            }.toMap()
        }
        val vectorTile = VectorTile.Tile.newBuilder().addLayers(
            ContourVectorTileMapper(contourOptions, linesByElevation).apply()
        ).build()
        repository.store(tile.id, "pbf", vectorTile.toByteArray())
        metricsProvider.get().addCount("VectorTile")
        return true
    }
}

fun vectorSchema(minZoom: Int, maxZoom: Int,contourLayer: String, levelName: String, elevationName: String): String = """
{
    "tilejson": "3.0.0",
    "format": "pbf",
    "maxzoom": $maxZoom,
    "minzoom": $minZoom,
    "scheme": "xyz",
    "vector_layers": [
        {
            "id": "$contourLayer",
            "description": "Elevation contour lines",
            "maxzoom": $maxZoom,
            "minzoom": $minZoom,
            "fields": {
                "$elevationName": "Integer. The contour line elevation in meters.",
                "$levelName": "Indicator for major or minor contour lines. A value of 1 indicates a major line, otherwise 0."
            }
        }
    ]
}
   """.trimIndent().trim()