package com.greensopinion.elevation.processor

class TileRange(
    val minZ: Int,
    val maxZ: Int,
    val minX: Int,
    val maxX: Int,
    val minY: Int,
    val maxY: Int,
) {
    init {
        require(maxZ >= minZ) { "maxZ=$maxZ must be greater or equal to minZ=$minZ" }
        val c = TileCoordinates(minZ)
        c.checkZ(minZ)
        c.checkZ(maxZ)
        c.checkX(minX)
        c.checkX(maxX)
        c.checkY(minY)
        c.checkY(maxY)
    }

    fun tiles(): Sequence<TileId> {
        return sequence {
            for (range in ranges()) {
                for (y in IntRange(range.minY, range.maxY)) {
                    for (x in IntRange(range.minX, range.maxX)) {
                        yield(TileId(z = range.minZ, x = x, y = y))
                    }
                }
            }
        }
    }

    private fun ranges(): Sequence<TileRange> {
        return sequence {
            for (z in IntRange(minZ, maxZ)) {
                val scalingFactor = 1 shl (z - minZ)
                yield(
                    TileRange(
                        minX = minX * scalingFactor,
                        maxX = (maxX + 1) * scalingFactor - 1,
                        minY = minY * scalingFactor,
                        maxY = (maxY + 1) * scalingFactor - 1,
                        minZ = z, maxZ = z
                    )
                )
            }
        }
    }
}