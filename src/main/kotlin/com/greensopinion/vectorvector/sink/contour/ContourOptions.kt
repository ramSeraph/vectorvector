package com.greensopinion.vectorvector.sink.contour

class ContourOptions(
    /** Factor to scale elevation meters, to support different units */
    val multiplier: Double = 1.0,

    /** The key for the elevation property to set on each contour line. */
    val elevationKey: String = "ele",

    /** The key for the level property to set on each contour line. Minor levels have level=0 and
    major levels have level=1 */
    val levelKey: String = "level",

    /** The name of the vector tile layer for contour lines. */
    val contourLayer: String = "contours",

    /** The extent of the vector tile. */
    val extent: Int = 4096,

    /** The number of indices to generate into the neighbouring tile to reduce rendering artifacts. */
    val buffer: Int = 1,

    /** The threshold used for minor contour lines */
    val minorLevel: Int = 50,

    /** The threshold used for major contour lines */
    val majorLevel: Int = 200,

    /** The minimum level, exclusive, to include in contour lines */
    val minLevelExclusive: Int = 0
)