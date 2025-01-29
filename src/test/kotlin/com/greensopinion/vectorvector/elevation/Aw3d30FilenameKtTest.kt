package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class Aw3d30FilenameKtTest {
    @Test
    fun `provides filename south of the equator`() {
        val coordinates = Coordinates(-45.580655, 159.583307)
        assertThat(coordinates.to5x5Filename()).isEqualTo("S050E155_S045E160")
        assertThat(coordinates.to1x1Filename()).isEqualTo("S045E159")
    }

    @Test
    fun `provides filename north of the equator`() {
        val coordinates = Coordinates(39.6931698, -74.6969458)
        assertThat(coordinates.to5x5Filename()).isEqualTo("N035W075_N040W070")
        assertThat(coordinates.to1x1Filename()).isEqualTo("N039W074")
    }

    @Test
    fun `provides another filename north of the equator`() {
        val coordinates = Coordinates(49.2927777, -123.1673444)
        assertThat(coordinates.to5x5Filename()).isEqualTo("N045W125_N050W120")
        assertThat(coordinates.to1x1Filename()).isEqualTo("N049W123")
    }
}