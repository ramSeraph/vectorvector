package com.greensopinion.vectorvector

class TileCoordinates(val z: Int) {
    val minX = 0
    val minY = 0
    val maxX = (1 shl z) - 1
    val maxY = maxX

    fun checkZ(z: Int) {
        check("z", z, 0..25)
    }

    fun checkX(x: Int) {
        check("x", x, minX..maxX)
    }

    fun checkY(y: Int) {
        check("y", y, minY..maxY)
    }

    private fun check(name: String, v: Int, range: IntRange) {
        require(range.contains(v)) { "$name must be in $range" }
    }
}