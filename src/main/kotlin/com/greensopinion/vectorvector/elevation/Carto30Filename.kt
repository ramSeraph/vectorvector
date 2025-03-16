package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates

fun Coordinates.toCarto30Filename(): String {
    val gridID = carto30GridIDMap[this]
    return "cdn${gridID}.tif"
}
