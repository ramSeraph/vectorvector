package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import com.greensopinion.vectorvector.Elevation
import com.greensopinion.vectorvector.INVALID_ELEVATION
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt

class BlockMapperElevationIT {
    private val mapper = com.greensopinion.vectorvector.elevation.BlockMapper(
        blockExtent = 6000,
        blockSize = com.greensopinion.vectorvector.elevation.Degrees(5.0)
    )

    @Nested
    inner class `Common Locations` {
        @Test
        fun `provides elevation for Everest`() {
            assertElevation(Coordinates(27.989065, 86.925223),
                com.greensopinion.vectorvector.elevation.elevation(8_806)
            )
        }

        @Test
        fun `provides elevation for Mount Rainier`() {
            assertElevation(Coordinates(46.8523212, -121.7606441),
                com.greensopinion.vectorvector.elevation.elevation(4_371)
            )
        }

        @Test
        fun `provides elevation for Mount Kosciuszko`() {
            assertElevation(Coordinates(-36.4553792, 148.263399),
                com.greensopinion.vectorvector.elevation.elevation(2_220)
            )
        }

        @Test
        fun `provides elevation for Aconcagua`() {
            assertElevation(Coordinates(-32.65256, -70.0108672),
                com.greensopinion.vectorvector.elevation.elevation(6_928)
            )
        }

        @Test
        fun `provides elevation for Haleakala`() {
            assertElevation(Coordinates(20.7084082, -156.2558971),
                com.greensopinion.vectorvector.elevation.elevation(3_046)
            )
        }
    }

    @Nested
    inner class `Sea Level` {

        @Test
        fun `provides elevation near Seattle and Vancouver`() {
            assertElevation(Coordinates(48.8153456, -123.3630877),
                com.greensopinion.vectorvector.elevation.elevation(0)
            )
            assertElevation(Coordinates(49.300221, -122.939081), com.greensopinion.vectorvector.elevation.elevation(0))
        }

        @Test
        fun `provides elevation near San Francisco`() {
            assertElevation(Coordinates(37.842456, -122.406336), com.greensopinion.vectorvector.elevation.elevation(0))
        }

        @Test
        fun `provides elevation near Akimiski Island`() {
            assertElevation(Coordinates(53.433667, -81.338326), com.greensopinion.vectorvector.elevation.elevation(0))
        }
    }

    @Nested
    inner class `Missing Elevation Data` {

        @Test
        fun `provides elevation near Tuktoyaktuk`() {
            assertNoElevation(Coordinates(69.444865, -133.091043))
        }

        @Test
        fun `provides elevation in the Beaufort Sea`() {
            assertNoElevation(Coordinates(68.942646, -136.887760))
        }
    }

    @Nested
    inner class `Tile Corners`() {
        @Test
        fun `tile 11 324 700`() {
            assertElevation(Coordinates(49.38237278700955, -123.046875),
                com.greensopinion.vectorvector.elevation.elevation(578)
            )
            assertElevation(Coordinates(49.26825260148868, -123.046875),
                com.greensopinion.vectorvector.elevation.elevation(60)
            )
            assertElevation(Coordinates(49.38237278700955, -122.87178039550781),
                com.greensopinion.vectorvector.elevation.elevation(0)
            )
            assertElevation(Coordinates(49.26825260148868, -122.87178039550781),
                com.greensopinion.vectorvector.elevation.elevation(132)
            )
        }
    }

    private fun assertNoElevation(location: Coordinates) = assertElevation(location, INVALID_ELEVATION)
    private fun assertElevation(location: Coordinates, expected: Elevation) {
        val blockOffset = mapper.map(location)
        val block = com.greensopinion.vectorvector.elevation.testBlockStore.load(blockOffset.blockId)
        val elevation = block.get(blockOffset.position.x.roundToInt(), blockOffset.position.y.roundToInt())
        assertThat(elevation.meters).isCloseTo(expected.meters, Offset.offset(2.0))
    }
}

private fun elevation(meters: Int) = Elevation(meters = meters.toDouble())