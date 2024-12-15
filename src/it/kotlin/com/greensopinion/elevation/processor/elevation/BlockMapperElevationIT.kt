package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.math.roundToInt

class BlockMapperElevationIT {
    private val mapper = BlockMapper(blockExtent = 6000, blockSize = Degrees(5.0))
    private val store = CachingBlockStore(
        FilesystemBlockStore(
            folder = File("../../data/tif")
        )
    )

    @Nested
    inner class `Common Locations` {
        @Test
        fun `provides elevation for Everest`() {
            assertElevation(Coordinates(27.989065, 86.925223), Elevation(8_806.0))
        }

        @Test
        fun `provides elevation for Mount Rainier`() {
            assertElevation(Coordinates(46.8523212, -121.7606441), Elevation(4_371.0))
        }

        @Test
        fun `provides elevation for Mount Kosciuszko`() {
            assertElevation(Coordinates(-36.4553792, 148.263399), Elevation(2_220.0))
        }

        @Test
        fun `provides elevation for Aconcagua`() {
            assertElevation(Coordinates(-32.65256, -70.0108672), Elevation(6_928.0))
        }

        @Test
        fun `provides elevation for Haleakala`() {
            assertElevation(Coordinates(20.7084082, -156.2558971), Elevation(3_046.0))
        }
    }

    @Nested
    inner class `Sea Level` {

        @Test
        fun `provides elevation near Seattle and Vancouver`() {
            assertElevation(Coordinates(48.8153456, -123.3630877), Elevation(0.0))
            assertElevation(Coordinates(49.300221, -122.939081), Elevation(0.0))
        }

        @Test
        fun `provides elevation near San Francisco`() {
            assertElevation(Coordinates(37.842456, -122.406336), Elevation(0.0))
        }

        @Test
        fun `provides elevation near Akimiski Island`() {
            assertElevation(Coordinates(53.433667, -81.338326), Elevation(0.0))
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
            assertElevation(Coordinates(49.38237278700955,  -123.046875), Elevation(578.0))
            assertElevation(Coordinates(49.26825260148868,  -123.046875), Elevation(60.0))
            assertElevation(Coordinates(49.38237278700955, -122.87178039550781), Elevation(0.0))
            assertElevation(Coordinates(49.26825260148868, -122.87178039550781), Elevation(132.0))
        }
    }

    private fun assertNoElevation(location: Coordinates) = assertElevation(location, INVALID_ELEVATION)
    private fun assertElevation(location: Coordinates, expected: Elevation) {
        val blockOffset = mapper.map(location)
        val block = store.load(blockOffset.blockId)
        val elevation = block.get(blockOffset.position.x.roundToInt(), blockOffset.position.y.roundToInt())
        assertThat(elevation.meters).isCloseTo(expected.meters, Offset.offset(2.0))
    }
}