package com.greensopinion.elevation.processor.elevation

import kotlin.math.min


@JvmInline
value class Degrees(val degrees: Double)

data class BlockId(val x: Int, val y: Int)

data class Offset(
    val x: Double,
    val y: Double
) {
    fun clamp(maximum: Double): Offset = Offset(x = min(maximum, x), y = min(maximum, y))
}

data class BlockOffset(
    val blockId: BlockId,
    val position: Offset
)

