package com.greensopinion.vectorvector

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TileRangeTest {
    @Test
    fun `provides a tile range for zoom level 0`() {
        val range = TileRange(minZ = 0, maxZ = 0, minX = 0, maxX = 0, minY = 0, maxY = 0)
        val tiles = range.tiles().toList()
        assertThat(tiles).containsExactly(TileId(0, 0, 0))
    }

    @Test
    fun `provides a tile range for zoom level 1`() {
        val range = TileRange(minZ = 1, maxZ = 1, minX = 0, maxX = 1, minY = 0, maxY = 1)
        val tiles = range.tiles().toList()
        assertThat(tiles).containsExactlyInAnyOrder(
            TileId(z=1, x=0, y=0),
            TileId(z=1, x=1, y=0),
            TileId(z=1, x=0, y=1),
            TileId(z=1, x=1, y=1)
        )
    }

    @Test
    fun `provides a tile range for zoom level 1-2`() {
        val range = TileRange(minZ = 1, maxZ = 2, minX = 0, maxX = 1, minY = 0, maxY = 1)
        val tiles = range.tiles().toList()
        assertThat(tiles).containsExactlyInAnyOrder(
            TileId(z=1, x=0, y=0),
            TileId(z=1, x=1, y=0),
            TileId(z=1, x=0, y=1),
            TileId(z=1, x=1, y=1),
            TileId(z=2, x=0, y=0),
            TileId(z=2, x=1, y=0),
            TileId(z=2, x=2, y=0),
            TileId(z=2, x=3, y=0),
            TileId(z=2, x=0, y=1),
            TileId(z=2, x=1, y=1),
            TileId(z=2, x=2, y=1),
            TileId(z=2, x=3, y=1),
            TileId(z=2, x=0, y=2),
            TileId(z=2, x=1, y=2),
            TileId(z=2, x=2, y=2),
            TileId(z=2, x=3, y=2),
            TileId(z=2, x=0, y=3),
            TileId(z=2, x=1, y=3),
            TileId(z=2, x=2, y=3),
            TileId(z=2, x=3, y=3)
        )
    } @Test
    fun `provides a tile range in depth-first order`() {
        val range = TileRange(minZ = 1, maxZ = 2, minX = 0, maxX = 1, minY = 0, maxY = 1)
        val tiles = range.tiles().toList()
        assertThat(tiles).containsExactly(
            TileId(z=1, x=0, y=0),
            TileId(z=2, x=0, y=0),
            TileId(z=2, x=0, y=1),
            TileId(z=2, x=1, y=0),
            TileId(z=2, x=1, y=1),
            TileId(z=1, x=1, y=0),
            TileId(z=2, x=2, y=0),
            TileId(z=2, x=2, y=1),
            TileId(z=2, x=3, y=0),
            TileId(z=2, x=3, y=1),
            TileId(z=1, x=0, y=1),
            TileId(z=2, x=0, y=2),
            TileId(z=2, x=0, y=3),
            TileId(z=2, x=1, y=2),
            TileId(z=2, x=1, y=3),
            TileId(z=1, x=1, y=1),
            TileId(z=2, x=2, y=2),
            TileId(z=2, x=2, y=3),
            TileId(z=2, x=3, y=2),
            TileId(z=2, x=3, y=3)
        )
    }

    @Test
    fun `provides a size`() {
        val range = TileRange(minZ = 1, maxZ = 2, minX = 0, maxX = 1, minY = 0, maxY = 1)
        assertThat(range.size).isEqualTo(20)
    }
    @Test
    fun `provides a size across a wider range`() {
        val range = TileRange(minZ = 10, maxZ = 12, minX = 50, maxX = 55, minY = 80, maxY = 85)
        assertThat(range.size).isEqualTo(756)
    }
}