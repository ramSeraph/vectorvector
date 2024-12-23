package com.greensopinion.elevation.processor.elevation

import com.greensopinion.elevation.processor.Tile
import com.greensopinion.elevation.processor.TileId
import com.greensopinion.elevation.processor.metrics.SingletonMetricsProvider
import com.greensopinion.elevation.processor.repository.FilesystemTileRepository
import com.greensopinion.elevation.processor.sink.VectorTileSink
import com.greensopinion.elevation.processor.sink.contour.ContourOptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class SingleVectorTileIT {
    val outputDir = File("target/tmp/${javaClass.simpleName}").also { it.mkdirs() }
    val tileExtent = 256
    val sink = VectorTileSink(
        contourOptionsProvider = { ContourOptions(minorLevel = 20, majorLevel = 100) },
        elevationDataStore = BlockElevationDataStore(
            blockSize = Degrees(5.0),
            blockExtent = 6000,
            tileExtent = tileExtent,
            blockStore = testBlockStore
        ),
        repository = FilesystemTileRepository(outputDir),
        metricsProvider = SingletonMetricsProvider()
    )

    @Test
    fun `creates a vector tile with contours`() {
        val tileId = TileId(12, 646, 1400)
        assertTile(tileId)
    }

    @Test
    fun `creates a vector tile with contours at 9 14 148`() {
        val tileId = TileId(9, 14, 148)
        assertTile(tileId)
    }

    private fun assertTile(tileId: TileId) {
        assertThat(sink.accept(Tile(tileId))).isTrue()
        val tile = File(outputDir, "${tileId.z}/${tileId.x}/${tileId.y}.pbf")
        assertThat(tile.exists()).isTrue()
    }
}