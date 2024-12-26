package com.greensopinion.vectorvector.sink.hillshade

import com.greensopinion.vectorvector.TileId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset.offset
import org.junit.jupiter.api.Test

class ResolutionPerPixelTest {

    private val resolution = ResolutionPerPixel(tileExtent = 256, minZ = 6, maxZ = 12)

    @Test
    fun `provides resolution per pixel at a zoom level near oslo`() {
        val tile = TileId(10, 0, 297)
        assertThat(resolution.metersPerPixel(tile)).isCloseTo(76.49, offset(0.001))
    }

    @Test
    fun `provides resolution per pixel at a zoom level near vancouver`() {
        val tile = TileId(11, 0, 700)
        assertThat(resolution.metersPerPixel(tile)).isCloseTo(49.819, offset(0.001))
    }
}