package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SrtmBlockMapperTest {
    val mapper = SrtmBlockMapper(
        blockSize = Degrees(5.0),
        blockExtent = 6000
    )

    @Test
    fun `provides degrees per pixel`() {
        assertThat(mapper.pixelSize.degrees).isCloseTo(0.0008333, Offset.offset(0.000001))
    }

    @Nested
    inner class `Block Id` {
        @Test
        fun `provides block offset on the left`() {
            assertBlock(-30.0, -180.0, BlockId(1, 19))
            assertBlock(-34.999, -180.0, BlockId(1, 19))

            assertBlock(55.0, -180.0, BlockId(1, 2))
            assertBlock(50.01, -180.0, BlockId(1, 2))

            assertBlock(60.0, -165.0, BlockId(4, 1))
            assertBlock(55.01, -165.0, BlockId(4, 1))
            assertBlock(55.0, -165.0, BlockId(4, 2))
            assertBlock(50.01, -165.0, BlockId(4, 2))

        }

        @Test
        fun `provides block offset near the center`() {
            assertBlock(-45.0, 65.0, BlockId(50, 22))
            assertBlock(-49.999, 69.999, BlockId(50, 22))

            assertBlock(10.0, 25.0, BlockId(42, 11))
            assertBlock(5.001, 25.0, BlockId(42, 11))
            assertBlock(10.0, 29.999, BlockId(42, 11))
            assertBlock(5.001, 29.999, BlockId(42, 11))
        }

        @Test
        fun `provides block offset on the right`() {
            assertBlock(55.0, 175.0, BlockId(72, 2))
            assertBlock(50.01, 175.0, BlockId(72, 2))
            assertBlock(55.0, 179.999, BlockId(72, 2))
            assertBlock(50.01, 179.999, BlockId(72, 2))
        }
    }

    @Nested
    inner class `Reverse Map` {
        @Test
        fun `reverse maps coordinates to a block`() {
            assertReverseMap(55.0, 175.0, BlockId(72, 2))
            assertReverseMap(55.0, 180.0, BlockId(73, 2))
            assertReverseMap(50.0, 180.0, BlockId(73, 3))
            assertReverseMap(10.0, 25.0, BlockId(42, 11))
            assertReverseMap(-45.0, 65.0, BlockId(50, 22))
            assertReverseMap(-30.0, -180.0, BlockId(1, 19))
            assertReverseMap(-35.0, -180.0, BlockId(1, 20))
            assertReverseMap(-35.0, -175.0, BlockId(2, 20))
        }

        private fun assertReverseMap(latitude: Double, longitude: Double, blockId: BlockId) {
            val reverseMapping = mapper.reverseMap(blockId)
            assertThat(reverseMapping.latitude).isCloseTo(latitude, Offset.offset(0.01))
            assertThat(reverseMapping.longitude).isCloseTo(longitude, Offset.offset(0.01))
        }
    }

    @Nested
    inner class `Pixel Offset` {
        @Test
        fun `provides a pixel offset bottom left`() {
            val blockOffset = mapper.map(Coordinates(50.000001,5.0))
            assertThat(blockOffset.blockId).isEqualTo(BlockId(38,2))
            assertThat(blockOffset.position.x).isCloseTo(0.0,Offset.offset(0.1))
            assertThat(blockOffset.position.y).isCloseTo(5999.0,Offset.offset(0.1))
        }

        @Test
        fun `provides a pixel offset top right`() {
            val blockOffset = mapper.map(Coordinates(55.0,9.999999))
            assertThat(blockOffset.blockId).isEqualTo(BlockId(38,2))
            assertThat(blockOffset.position.x).isCloseTo(5999.0,Offset.offset(0.1))
            assertThat(blockOffset.position.y).isCloseTo(0.0,Offset.offset(0.1))
        }

        @Test
        fun `provides a pixel offset top left`() {
            val blockOffset = mapper.map(Coordinates(49.999999,5.0))
            assertThat(blockOffset.blockId).isEqualTo(BlockId(38,3))
            assertThat(blockOffset.position.x).isCloseTo(0.0,Offset.offset(0.1))
            assertThat(blockOffset.position.y).isCloseTo(0.0,Offset.offset(0.1))
        }

        @Test
        fun `provides a pixel offset center`() {
            val blockOffset = mapper.map(Coordinates(52.5,7.5))
            assertThat(blockOffset.blockId).isEqualTo(BlockId(38,2))
            assertThat(blockOffset.position.x).isCloseTo(3000.0,Offset.offset(0.1))
            assertThat(blockOffset.position.y).isCloseTo(3000.0,Offset.offset(0.1))
        }
    }

    @Nested
    inner class `Map Area`() {
        @Test
        fun `maps an area`() {
            assertThat(mapper.mapArea(Coordinates(49.051178, -123.147672), Coordinates(53.484352, -117.306286)))
                .containsExactlyInAnyOrder(
                    BlockId(x = 12, y = 4), BlockId(x = 13, y = 4), BlockId(x = 12, y = 3), BlockId(x = 13, y = 3)
                )
        }

        @Test
        fun `maps an area crossing the antimeridian`() {
            assertThat(mapper.mapArea(Coordinates(-1.385162, 177.309593), Coordinates(1.624046, -177.398017)))
                .containsExactlyInAnyOrder(
                    BlockId(x = 72, y = 14),
                    BlockId(x = 1, y = 14),
                    BlockId(x = 72, y = 13),
                    BlockId(x = 1, y = 13)
                )
        }
    }


    private fun assertBlock(latitude: Double, longitude: Double, blockId: BlockId) {
        val coordinates = Coordinates(latitude, longitude)
        val offset = mapper.map(coordinates)
        assertThat(offset.blockId).describedAs("$coordinates").isEqualTo(blockId)
    }
}