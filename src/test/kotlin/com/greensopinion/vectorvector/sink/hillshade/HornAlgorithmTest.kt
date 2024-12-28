package com.greensopinion.vectorvector.sink.hillshade

import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.EmptyTile
import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.elevation.ElevationDataStore
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HornAlgorithmTest {
    private val extent = 256
    private val tileId = TileId(10, 1, 2)

    @Test
    fun `provides hillshade on flat terrain`() {
        val horn = horn { x, y -> 0.0 }
        val tile = horn.create(tileId)
        assertThat(tile.extent).isEqualTo(256)
        (0..<tile.extent).forEach { x ->
            (0..<tile.extent).forEach { y ->
                val expected = if (x == 0 || x == 255 || y == 0 || y == 255) 255.0 else 180.0
                assertThat(tile.get(x, y).meters).isCloseTo(expected, Offset.offset(0.5))
            }
        }
    }

    @Test
    fun `provides hillshade on terrain sloping north west`() {
        val meters = 1000.0
        val fraction = { offset: Int -> (offset.toDouble() / extent) }
        val horn = horn { x, y -> fraction(x) * meters + fraction(y) * meters }
        val tile = horn.create(tileId)
        assertThat(tile.extent).isEqualTo(256)
        (1..<tile.extent - 1).forEach { x ->
            (1..<tile.extent - 1).forEach { y ->
                assertThat(tile.get(x, y).meters).isCloseTo(235.4, Offset.offset(0.5))
            }
        }
    }

    @Test
    fun `provides hillshade on terrain sloping south east`() {
        val meters = 1000.0
        val fraction = { offset: Int -> (offset.toDouble() / extent) }
        val horn = horn { x, y -> (2 * meters) - (fraction(x) * meters + fraction(y) * meters) }
        val tile = horn.create(tileId)
        assertThat(tile.extent).isEqualTo(256)
        (1..<tile.extent - 1).forEach { x ->
            (1..<tile.extent - 1).forEach { y ->
                assertThat(tile.get(x, y).meters).isCloseTo(97.9, Offset.offset(0.5))
            }
        }
    }

    private fun horn(elevation: (x: Int, y: Int) -> Double): HornAlgorithm {
        val tile = mockTile(elevation)
        val dataStore = mock<ElevationDataStore>().also {
            doReturn(EmptyTile(tile.extent)).whenever(it).get(any())
            doReturn(tile).whenever(it).get(tileId)
        }
        val resolutionPerPixel = ResolutionPerPixel(tile.extent, 6, 12)
        return HornAlgorithm(
            dataStore, resolutionPerPixel
        )
    }

    private fun mockTile(elevation: (x: Int, y: Int) -> Double): ElevationTile = mock<ElevationTile>().also {

        doAnswer { invocation ->
            val x = invocation.getArgument<Int>(0)
            val y = invocation.getArgument<Int>(1)
            elevation(x, y)
        }.whenever(it).get(any(), any())
        doReturn(extent).whenever(it).extent
    }
}