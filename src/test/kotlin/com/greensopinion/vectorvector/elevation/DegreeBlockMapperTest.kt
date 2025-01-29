package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DegreeBlockMapperTest {
    private val mapper = DegreeBlockMapper(blockExtent = 3600, blockSize = Degrees(1.0))


    @Test
    fun `provides degrees per pixel`() {
        assertThat(mapper.pixelSize.degrees).isCloseTo(0.0002777, Offset.offset(0.000001))
    }

    @Nested
    inner class `Block Id` {
        @Test
        fun `provides block offset on the left`() {
            assertBlock(-30.0, -180.0, BlockId(-180, -30))
            assertBlock(-34.999, -180.0, BlockId(-180, -35))

            assertBlock(55.0, -180.0, BlockId(-180, 55))
            assertBlock(50.01, -180.0, BlockId(-180, 50))

            assertBlock(60.0, -165.0, BlockId(-165, 60))
            assertBlock(55.01, -165.0, BlockId(-165, 55))
            assertBlock(55.0, -165.0, BlockId(-165, 55))
            assertBlock(50.01, -165.0, BlockId(-165, 50))

        }

        @Test
        fun `provides block offset near the center`() {
            assertBlock(-45.0, 65.0, BlockId(65, -45))
            assertBlock(-49.999, 69.999, BlockId(69, -50))

            assertBlock(10.0, 25.0, BlockId(25, 10))
            assertBlock(5.001, 25.0, BlockId(25, 5))
            assertBlock(10.0, 29.999, BlockId(29, 10))
            assertBlock(5.001, 29.999, BlockId(29, 5))
        }

        @Test
        fun `provides block offset on the right`() {
            assertBlock(55.0, 175.0, BlockId(175, 55))
            assertBlock(50.01, 175.0, BlockId(175, 50))
            assertBlock(55.0, 179.999, BlockId(179, 55))
            assertBlock(50.01, 179.999, BlockId(179, 50))
        }
    }

    @Nested
    inner class `Reverse Mapping` {
        @Test
        fun `reverse maps a block ID`() {
            assertThat(mapper.reverseMap(BlockId(170, 55))).isEqualTo(Coordinates(55.0, 170.0))
        }
    }

    @Nested
    inner class `Map Area`() {
        @Test
        fun `maps an area`() {
            assertThat(mapper.mapArea(Coordinates(49.051178, -123.147672), Coordinates(53.484352, -117.306286)))
                .containsExactlyInAnyOrderElementsOf(
                    (49..53).flatMap { lat -> (-124..-118).map { lon -> BlockId(x = lon, y = lat) } }.toList()
                )
        }

        @Test
        fun `maps another area`() {
            assertThat(mapper.mapArea(Coordinates(49.051178, -123.147672), Coordinates(53.484352, -117.306286)))
                .containsExactlyInAnyOrderElementsOf(
                    (49..53).flatMap { lat -> (-124..-118).map { lon -> BlockId(x = lon, y = lat) } }.toList()
                )

        }

        @Test
        fun `maps an area crossing the antimeridian`() {
            assertThat(mapper.mapArea(Coordinates(-1.385162, 177.309593), Coordinates(1.624046, -177.398017)))
                .containsExactlyInAnyOrderElementsOf(
                    listOf(
                        BlockId(x = 177, y = -2),
                        BlockId(x = 178, y = -2),
                        BlockId(x = 179, y = -2),
                        BlockId(x = -180, y = -2),
                        BlockId(x = -179, y = -2),
                        BlockId(x = -178, y = -2),
                        BlockId(x = 177, y = -1),
                        BlockId(x = 178, y = -1),
                        BlockId(x = 179, y = -1),
                        BlockId(x = -180, y = -1),
                        BlockId(x = -179, y = -1),
                        BlockId(x = -178, y = -1),
                        BlockId(x = 177, y = 0),
                        BlockId(x = 178, y = 0),
                        BlockId(x = 179, y = 0),
                        BlockId(x = -180, y = 0),
                        BlockId(x = -179, y = 0),
                        BlockId(x = -178, y = 0),
                        BlockId(x = 177, y = 1),
                        BlockId(x = 178, y = 1),
                        BlockId(x = 179, y = 1),
                        BlockId(x = -180, y = 1),
                        BlockId(x = -179, y = 1),
                        BlockId(x = -178, y = 1)
                    )
                )
        }
    }

    private fun assertBlock(latitude: Double, longitude: Double, blockId: BlockId) {
        val coordinates = Coordinates(latitude, longitude)
        val offset = mapper.map(coordinates)
        assertThat(offset.blockId).describedAs("$coordinates").isEqualTo(blockId)
    }
}