package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Position
import com.greensopinion.elevation.processor.TileId
import com.greensopinion.elevation.processor.TilePosition
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SlippyMapTranslatorTest {
    private val tileExtent = 256
    private val translator = SlippyMapTranslator(tileExtent)

    @Test
    fun `provides coordinates for a tile at the tile origin`() {
        val equivalentPositions = listOf(
            TilePosition(
                tile = TileId(z = 11, x = 325, y = 703),
                position = Position(x = 0, y = 0)
            ),
            TilePosition(
                tile = TileId(z = 15, x = 5200, y = 11248),
                position = Position(x = 0, y = 0)
            ),
            TilePosition(
                tile = TileId(z = 17, x = 20800, y = 44992),
                position = Position(x = 0, y = 0)
            )
        )
        for (position in equivalentPositions) {
            val coordinates = translator.map(position)
            assertThat(coordinates.latitude).describedAs("$position => $coordinates")
                .isCloseTo(49.0378679, Offset.offset(0.0000001))
            assertThat(coordinates.longitude).describedAs("$position => $coordinates")
                .isCloseTo(-122.87109375, Offset.offset(0.0000001))
        }
    }

    @Test
    fun `provides coordinates for a tile at the top right`() {
        val position = TilePosition(
            tile = TileId(z = 15, x = 5200, y = 11248),
            position = Position(x = tileExtent-1, y = tileExtent-1)
        )
        val coordinates = translator.map(position)
        assertThat(coordinates.latitude).describedAs("$position => $coordinates")
            .isCloseTo(49.03069336, Offset.offset(0.0000001))
        assertThat(coordinates.longitude).describedAs("$position => $coordinates")
            .isCloseTo(-122.86015033, Offset.offset(0.0000001))
    }

}