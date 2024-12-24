package com.greensopinion.vectorvector.elevation

import kotlin.math.min


@JvmInline
value class Degrees(val degrees: Double)

data class BlockId(val x: Int, val y: Int) {
    val valid : Boolean get() = x >= 0 && y >= 0
}

data class Offset(
    val x: Double,
    val y: Double
) {
    fun clamp(maximum: Double): Offset = Offset(x = min(maximum, x), y = min(maximum, y))

    fun floor() = Offset(kotlin.math.floor(x), kotlin.math.floor(y))
    fun ceil() = Offset(kotlin.math.ceil(x), kotlin.math.ceil(y))
}

data class BlockOffset(
    val blockId: BlockId,
    val position: Offset
)

