package com.greensopinion.vectorvector.repository

import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.metrics.SingletonMetricsProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.random.Random

class MbtilesTileRepositoryTest {
    private val file = File("target/tmp/${javaClass.simpleName}/example.mbtiles")
    private val metricsProvider = SingletonMetricsProvider()

    @BeforeEach
    fun setup() {
        file.parentFile.mkdirs()
        file.delete()
    }

    @Test
    fun `can create a file`() {
        assertThat(file).doesNotExist()
        val tile = TileId(6, 10, 21)
        val bytes = ByteArray(256).also {
            Random(System.currentTimeMillis()).nextBytes(it)
        }
        MbtilesTileRepository(file, metricsProvider).use {
            it.store(tile, "pbf", bytes)
        }
        assertThat(file).exists()
        MbtilesTileRepository(file, metricsProvider, truncate = false).use {
            val tileBytes = it.read(tile).get()
            assertThat(tileBytes).isEqualTo(bytes)
        }
    }
}