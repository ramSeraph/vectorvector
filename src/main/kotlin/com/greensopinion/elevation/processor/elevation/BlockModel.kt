package com.greensopinion.elevation.processor.elevation


@JvmInline
value class Degrees(val degrees: Double)

data class BlockId(val x: Int, val y: Int)

data class Offset(
    val x: Double,
    val y: Double
)

data class BlockOffset(
    val blockId: BlockId,
    val position: Offset
)

