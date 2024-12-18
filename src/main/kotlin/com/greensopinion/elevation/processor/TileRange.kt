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

    val size: Int get() {
        var count = 0
        val baseRangeX = maxX-minX+1
        val baseRangeY = maxY-minY+1

        for (z in minZ..maxZ) {
            val factor = 1 shl (z - minZ)
            val rangeX = baseRangeX * factor
            val rangeY = baseRangeY* factor
            count += (rangeX*rangeY)
        }
        return count
    }

    fun tiles(): Sequence<TileId> {
        return sequence {
            for (y in IntRange(minY, maxY)) {
                for (x in IntRange(minX, maxX)) {
                    for (z in IntRange(minZ, maxZ)) {
                        val scalingFactor = 1 shl (z - minZ)
                        val innerMinX = x * scalingFactor
                        val innerMinY = y * scalingFactor
                        val innerMaxX = (x + 1) * scalingFactor - 1
                        val innerMaxY = (y + 1) * scalingFactor - 1
                        for (innerX in innerMinX..innerMaxX) {
                            for (innerY in innerMinY..innerMaxY) {
                                yield(TileId(z, innerX, innerY))
                            }
                        }
                    }
                }
            }
        }
    }
}