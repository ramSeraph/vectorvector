package com.greensopinion.elevation.processor.tile

import vector_tile.VectorTile
import kotlin.math.max
import kotlin.math.min

class TileModel(val layers: List<TileLayer>)
class TileLayer(
    val extent: Int,
    val name: String,
    val features: List<TileFeature>
)

data class Rectangle(val left: Int, val top: Int, val right: Int, val bottom: Int)
class Point(val x: Int, val y: Int)
class Line(val points: List<Point>) {
    fun bounds(): Rectangle {
        var l = Int.MAX_VALUE
        var r = Int.MIN_VALUE
        var t = Int.MAX_VALUE
        var b = Int.MIN_VALUE
        for (point in points) {
            l = min(point.x, l)
            r = max(point.x, r)
            t = min(point.y, t)
            b = max(point.y, b)
        }
        return Rectangle(left = l, right = r, top = t, bottom = b)
    }
}

class TileFeature(
    val type: VectorTile.Tile.GeomType,
    val properties: Map<String, Any>,
    val lines: List<Line>
)

class TileModelReader {
    fun read(tile: VectorTile.Tile): TileModel {
        return TileModel(
            layers = tile.layersList.map { toLayer(it) }
        )
    }

    private fun toLayer(layer: VectorTile.Tile.Layer): TileLayer {
        return TileLayer(
            extent = layer.extent,
            name = layer.name,
            features = layer.featuresList.map { toFeature(layer, it) }
        )
    }

    private fun toFeature(layer: VectorTile.Tile.Layer, feature: VectorTile.Tile.Feature): TileFeature {
        require(feature.type == VectorTile.Tile.GeomType.LINESTRING) { "unsupported type: ${feature.type}" }
        return TileFeature(
            type = feature.type,
            properties = toProperties(layer, feature),
            lines = toLines(feature.geometryList)
        )
    }

    private fun toLines(
        commands: List<Int>
    ): List<Line> {
        val lines = mutableListOf<Line>()
        var cx = 0
        var cy = 0
        val it = commands.iterator()
        while (it.hasNext()) {
            val points = mutableListOf<Point>()
            val moveToCommand = it.next()
            check(decodeCommand(moveToCommand) == Commands.moveTo)
            check(decodeCommandLength(moveToCommand) == 1)

            check(it.hasNext())
            cx += decodeZigZag(it.next())
            check(it.hasNext())
            cy += decodeZigZag(it.next())

            points.add(Point(cx, cy))

            check(it.hasNext())
            val lineToCommand = it.next()
            check(decodeCommand(lineToCommand) == Commands.lineTo)
            val segments = decodeCommandLength(lineToCommand)
            check(segments > 0)
            for (i in 0..<segments) {
                check(it.hasNext())
                cx += decodeZigZag(it.next())
                check(it.hasNext())
                cy += decodeZigZag(it.next())
                points.add(Point(cx, cy))
            }
            lines.add(Line(points))
        }
        return lines
    }

    private fun toProperties(layer: VectorTile.Tile.Layer, feature: VectorTile.Tile.Feature): Map<String, Any> {
        val properties = mutableMapOf<String, Any>()
        for (x in 0..<feature.tagsCount step 2) {
            val name = layer.keysList[feature.getTags(x)]
            val value = layer.getValues(feature.getTags(x + 1)).javaValue()
            properties[name] = value
        }
        return properties
    }
}

fun VectorTile.Tile.Value.javaValue(): Any {
    if (hasIntValue()) {
        return intValue
    } else if (hasSintValue()) {
        return sintValue
    } else if (hasDoubleValue()) {
        return doubleValue
    } else if (hasStringValue()) {
        return stringValue
    } else if (hasFloatValue()) {
        return floatValue
    } else if (hasBoolValue()) {
        return boolValue
    } else if (hasUintValue()) {
        return uintValue
    }
    throw Exception("Unsupported value: $this")
}

private fun decodeCommand(command: Int): Int = command and 0x7

private fun decodeCommandLength(command: Int): Int = command shr 3

private fun decodeZigZag(value: Int): Int = ((value shr 1) xor -(value and 1))

private object Commands {
    const val moveTo = 1
    const val lineTo = 2
    const val closePath = 7
}