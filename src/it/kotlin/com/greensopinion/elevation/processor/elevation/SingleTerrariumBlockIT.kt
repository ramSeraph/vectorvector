package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Tile
import com.greensopinion.elevation.processor.TileId
import com.greensopinion.elevation.processor.metrics.SingletonMetricsProvider
import com.greensopinion.elevation.processor.sink.FilesystemTileRepository
import com.greensopinion.elevation.processor.sink.TerrariumSink
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import java.io.File

class SingleTerrariumBlockIT {
    val outputDir = File("target/tmp/${javaClass.simpleName}").also { it.mkdirs() }
    val tileExtent = 256
    val sink = TerrariumSink(
        elevationDataStore = BlockElevationDataStore(
            blockSize = Degrees(5.0),
            blockExtent = 6000,
            tileExtent = tileExtent,
            blockStore = testBlockStore
        ),
        extent = tileExtent,
        repository = FilesystemTileRepository(outputDir),
        metricsProvider = SingletonMetricsProvider()
    )

    @Test
    fun `creates a single Terrarium block in the ocean`() {
        val tileId = TileId(7, 60, 39)
        assertThat(sink.accept(Tile(tileId))).isTrue()
        val outputTile = TerrariumTileReader().read(File(outputDir, "${tileId.z}/${tileId.x}/${tileId.y}.png"))
        for (x in 0..<tileExtent) {
            for (y in 0..<tileExtent) {
                assertThat(outputTile.get(x, y).meters).isEqualTo(0.0)
            }
        }
    }

    @Test
    fun `creates a single Terrarium block on land`() {
        val tileId = TileId(8, 45, 99)
        assertThat(sink.accept(Tile(tileId))).isTrue()
        val outputTile = readOutput(tileId)
        val referenceTile = referenceTerrariumTile(tileId)
        for (x in 0..<tileExtent) {
            for (y in 0..<tileExtent) {
                assertThat(outputTile.get(x, y).meters).isCloseTo(referenceTile.get(x, y).meters, Offset.offset(250.0))
            }
        }
    }

    @Test
    fun `creates a single Terrarium block for 12, 646, 1400`() {
        val tileId = TileId(12, 646, 1400)
        assertThat(sink.accept(Tile(tileId))).isTrue()
        val outputTile = readOutput(tileId)
        val referenceTile = referenceTerrariumTile(tileId)
        for (x in 0..<tileExtent) {
            for (y in 0..<tileExtent) {
                assertThat(outputTile.get(x, y).meters).isCloseTo(referenceTile.get(x, y).meters, Offset.offset(150.0))
            }
        }
    }

    private fun readOutput(tileId: TileId) =
        TerrariumTileReader().read(File(outputDir, "${tileId.z}/${tileId.x}/${tileId.y}.png"))
}