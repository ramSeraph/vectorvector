package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import kotlin.math.abs
import kotlin.math.floor

fun Coordinates.to5x5Filename(): String {
    val degrees = 5

    val lowerLat = floor(latitude / degrees.toDouble()) * degrees
    val lowerLon = floor(longitude / degrees.toDouble()) * degrees

    val upperLat = lowerLat + degrees
    val upperLon = lowerLon + degrees

    val lowerLatPrefix = if (lowerLat >= 0) "N" else "S"
    val lowerLonPrefix = if (lowerLon >= 0) "E" else "W"
    val upperLatPrefix = if (upperLat >= 0) "N" else "S"
    val upperLonPrefix = if (upperLon >= 0) "E" else "W"

    val lowerLatStr = formatCoordinate(lowerLatPrefix, lowerLat.toInt())
    val lowerLonStr = formatCoordinate(lowerLonPrefix, lowerLon.toInt())
    val upperLatStr = formatCoordinate(upperLatPrefix, upperLat.toInt())
    val upperLonStr = formatCoordinate(upperLonPrefix, upperLon.toInt())

    return "${lowerLatStr}${lowerLonStr}_${upperLatStr}${upperLonStr}"
}

fun Coordinates.to1x1Filename(): String {
    // bottom left
    val latitudeSW = latitude.toInt()
    val longitudeSW = longitude.toInt()

    val swLatitude = if (latitudeSW >= 0) "N%03d".format(latitudeSW) else "S%03d".format(-latitudeSW)
    val swLongitude = if (longitudeSW >= 0) "E%03d".format(longitudeSW) else "W%03d".format(-longitudeSW)

    return "$swLatitude${swLongitude}"
}

private fun formatCoordinate(prefix: String, value: Int): String {
    val absValue = abs(value).toInt()
    return "%s%03d".format(prefix, absValue)
}

