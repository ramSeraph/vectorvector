package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.elevation.ElevationDataStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ElevationTileAreaTest {
    private var elevationSeed = 0
    private val tile = mockTile()
    private val tileId = TileId(6, 3, 3)
    private val left = mockTile()
    private val leftTop = mockTile()
    private val leftBottom = mockTile()
    private val top = mockTile()
    private val rightTop = mockTile()
    private val right = mockTile()
    private val rightBottom = mockTile()
    private val bottom = mockTile()
    private val store = mock<ElevationDataStore>().also {
        doReturn(tile).whenever(it).get(tileId)
        doReturn(left).whenever(it).get(tileId.copy(x = tileId.x - 1))
        doReturn(right).whenever(it).get(tileId.copy(x = tileId.x + 1))
        doReturn(bottom).whenever(it).get(tileId.copy(y = tileId.y + 1))
        doReturn(top).whenever(it).get(tileId.copy(y = tileId.y - 1))
        doReturn(leftTop).whenever(it).get(tileId.copy(x = tileId.x - 1, y = tileId.y - 1))
        doReturn(leftBottom).whenever(it).get(tileId.copy(x = tileId.x - 1, y = tileId.y + 1))
        doReturn(rightTop).whenever(it).get(tileId.copy(x = tileId.x + 1, y = tileId.y - 1))
        doReturn(rightBottom).whenever(it).get(tileId.copy(x = tileId.x + 1, y = tileId.y + 1))
    }
    private val tileWithNeighbours = ElevationTileArea(
        tileId, tile, store
    )

    @Test
    fun `provides elevation`() {
        assertElevation(0, 0, tile.get(0,0))
        assertElevation(-1, -1, leftTop.get(tile.extent-1,tile.extent-1))
        assertElevation(-1, 0, left.get(tile.extent-1,0))
        assertElevation(-1, tile.extent, leftBottom.get(tile.extent-1,0))
        assertElevation(0, -1, top.get(0,tile.extent-1))
        assertElevation(tile.extent, -1, rightTop.get(0,tile.extent-1))
        assertElevation(tile.extent, tile.extent, rightBottom.get(0,0))
        assertElevation(tile.extent, 0, right.get(0,0))
    }

    private fun assertElevation(x: Int, y: Int, expected: Elevation) {
        assertThat(tileWithNeighbours.get(x, y)).isEqualTo(expected)
    }

    private fun mockTile(): ElevationTile = mock<ElevationTile>().also {
        val extent = 256
        val elevationRange = extent * extent
        val originElevation = elevationSeed
        elevationSeed += elevationRange
        doAnswer { invoation ->
            val x = invoation.getArgument<Int>(0)
            val y = invoation.getArgument<Int>(1)
            originElevation + (y * extent) + x
        }.whenever(it).get(any(), any())
        doReturn(extent).whenever(it).extent
    }
}