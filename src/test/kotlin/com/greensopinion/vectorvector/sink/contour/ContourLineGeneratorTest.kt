package com.greensopinion.vectorvector.sink.contour

import com.greensopinion.vectorvector.Elevation
import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.EmptyTile
import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.elevation.ElevationDataStore
import com.greensopinion.vectorvector.tile.Rectangle
import com.greensopinion.vectorvector.tile.TileLayer
import com.greensopinion.vectorvector.tile.TileModel
import com.greensopinion.vectorvector.tile.TileModelReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import vector_tile.VectorTile
import java.io.File

class ContourLineGeneratorTest {
    val outputDir = File("target/tmp/${javaClass.simpleName}").also { it.mkdirs() }

    @Nested
    inner class Geometries {
        val midpoint = (tileSize / 2).toInt()
        val high = 21;
        val low = 10;

        @Test
        fun vertical() {
            val range = Pair(midpoint - 10, midpoint + 10)
            val tile = TestTile { x, y -> if (x >= range.first && x <= range.second) high else low }
            assertElevation("vertical", tile) { layer ->
                assertThat(layer.features.size).isEqualTo(2)
                val first = layer.features.first();
                assertThat(first.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(first.lines.size).isEqualTo(1)
                assertThat(first.lines.first().bounds()).isEqualTo(Rectangle(1887, 0, 1887, 4080))
                val second = layer.features.last();
                assertThat(second.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(second.lines.size).isEqualTo(1)
                assertThat(second.lines.first().bounds()).isEqualTo(Rectangle(2209, 0, 2209, 4080))
            }
        }

        @Test
        fun horizontal() {
            val range = Pair(midpoint - 10, midpoint + 10)
            val tile = TestTile { x, y -> if (y >= range.first && y <= range.second) high else low }
            assertElevation("horizontal", tile) { layer ->
                assertThat(layer.features.size).isEqualTo(2)
                val first = layer.features.first();
                assertThat(first.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(first.lines.size).isEqualTo(1)
                assertThat(first.lines.first().bounds()).isEqualTo(Rectangle(0, 1887, 4080, 1887))
                val second = layer.features.last();
                assertThat(second.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(second.lines.size).isEqualTo(1)
                assertThat(second.lines.first().bounds()).isEqualTo(Rectangle(0, 2209, 4080, 2209))
            }
        }

        @Test
        fun `diagonal top left to bottom right`() {
            fun inBounds(x: Int, y: Int) : Boolean {
                val range = Pair(x - 10, x + 10)
                return y >= range.first && y <= range.second;
            }
            val tile = TestTile { x, y -> if (inBounds(x,y)) high else low }
            assertElevation("diagonal-tl-br", tile) { layer ->
                assertThat(layer.features.size).isEqualTo(2)
                val first = layer.features.first();
                assertThat(first.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(first.lines.size).isEqualTo(1)
                assertThat(first.lines.first().bounds()).isEqualTo(Rectangle(161, 0, 4080, 3919))
                val second = layer.features.last();
                assertThat(second.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(second.lines.size).isEqualTo(1)
                assertThat(second.lines.first().bounds()).isEqualTo(Rectangle(0, 161, 3919, 4080))
            }
        }

        @Test
        fun `diagonal top right to bottom left`() {
            fun inBounds(x: Int, y: Int) : Boolean {
                val yCenter = tileSize - x
                val range = Pair(yCenter - 10, yCenter + 10)
                return y >= range.first && y <= range.second;
            }
            val tile = TestTile { x, y -> if (inBounds(x,y)) high else low }
            assertElevation("diagonal-tr-bl", tile) { layer ->
                assertThat(layer.features.size).isEqualTo(2)
                val first = layer.features.first();
                assertThat(first.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(first.lines.size).isEqualTo(1)
                assertThat(first.lines.first().bounds()).isEqualTo(Rectangle(0, 0, 3935, 3935))
                val second = layer.features.last();
                assertThat(second.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(second.lines.size).isEqualTo(1)
                assertThat(second.lines.first().bounds()).isEqualTo(Rectangle(177, 177, 4080, 4080))
            }
        }

        @Test
        fun `circle high`() {
            fun inBounds(x: Int, y: Int) : Boolean {
                val midpoint = tileSize / 2
                val radius = (tileSize * 0.1).toInt()
                val r2 = radius * radius
                val l = Math.pow((x - midpoint).toDouble(), 2.0) + Math.pow((y - midpoint).toDouble(), 2.0)
                return l <= r2;
            }
            val tile = TestTile { x, y -> if (inBounds(x,y)) high else low }
            assertElevation("circle-high", tile) { layer ->
                assertThat(layer.features.size).isEqualTo(1)
                val first = layer.features.first();
                assertThat(first.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(first.lines.size).isEqualTo(1)
                assertThat(first.lines.first().bounds()).isEqualTo(Rectangle(1647, 1647, 2449, 2449))
            }
        }
        @Test
        fun `circle low`() {
            fun inBounds(x: Int, y: Int) : Boolean {
                val midpoint = tileSize / 2
                val radius = (tileSize * 0.1).toInt()
                val r2 = radius * radius
                val l = Math.pow((x - midpoint).toDouble(), 2.0) + Math.pow((y - midpoint).toDouble(), 2.0)
                return l <= r2;
            }
            val tile = TestTile { x, y -> if (inBounds(x,y)) low else high }
            assertElevation("circle-low", tile) { layer ->
                assertThat(layer.features.size).isEqualTo(1)
                val first = layer.features.first();
                assertThat(first.type).isEqualTo(VectorTile.Tile.GeomType.LINESTRING)
                assertThat(first.lines.size).isEqualTo(1)
                assertThat(first.lines.first().bounds()).isEqualTo(Rectangle(left=1633, top=1633, right=2463, bottom=2463))
            }
        }
    }

    private fun assertLayer(tileModel: TileModel, layerName: String): TileLayer {
        val layer = tileModel.layers.firstOrNull { it.name == layerName }
        assertThat(layer).describedAs("$layerName in ${tileModel.layers.joinToString(", ") { it.name }}").isNotNull
        return layer!!
    }

    private fun assertElevation(name: String, tile: ElevationTile, assertions: (TileLayer) -> Unit) {
        val options = ContourOptions(minorLevel = 20)
        val tileId = TileId(8, 40, 87)
        val dataStore = mock<ElevationDataStore>().also {
            doReturn(EmptyTile(tile.extent)).whenever(it).get(any())
            doReturn(tile).whenever(it).get(tileId)
        }
        val linesByElevation = ContourLineGenerator(options, dataStore).generate(tileId)
        val vectorTile = VectorTile.Tile.newBuilder().addLayers(
            ContourVectorTileMapper(options, linesByElevation).apply()
        ).build()
        val bytes = vectorTile.toByteArray()
        File(outputDir, "tile-$name.pbf").writeBytes(bytes)
        val parsed = VectorTile.Tile.parseFrom(bytes)
        val tileModel = TileModelReader().read(parsed)
        assertions(assertLayer(tileModel, "contours"))
    }
}

private const val tileSize = 256

private class TestTile(
    val elevation: (x: Int, y: Int) -> Int
) : ElevationTile() {
    override val empty: Boolean = false
    override val extent: Int = tileSize

    override fun get(x: Int, y: Int): Elevation = Elevation(meters = elevation(x, y).toDouble())
}
