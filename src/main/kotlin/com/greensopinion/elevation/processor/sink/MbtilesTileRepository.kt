package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.TileId
import com.greensopinion.elevation.processor.metrics.MetricsProvider
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.imintel.mbtiles4j.MBTilesWriter
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


class MbtilesTileRepository(
    private val outputFile: File,
    private val metricsProvider: MetricsProvider,
    private val log: KLogger = KotlinLogging.logger { }
) : TileRepository {
    private val errors = CopyOnWriteArrayList<Exception>()
    private val maximumQueueSize = 200
    private val executor = ThreadPoolExecutor(
        1, 1,
        0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue<Runnable>(maximumQueueSize),
        { r ->
            Thread(r).also {
                it.name = "Mbtiles-writer-${threadIdSeed.incrementAndGet()}"
            }
        },
        { r, e ->
            e.queue.put(r)
        }
    )

    init {
        outputFile.delete()
        outputFile.parentFile.mkdirs()
    }

    private val writer: MBTilesWriter = MBTilesWriter(outputFile)

    override fun store(tile: TileId, extension: String, bytes: ByteArray) {
        executor.submit { write(tile, bytes) }
    }

    private fun write(tile: TileId, bytes: ByteArray) {
        try {
            writer.addTile(bytes, tile.z.toLong(), tile.x.toLong(), tile.y.toLong())
            metricsProvider.get().addCount("Mbtiles.written")
        } catch (e: Exception) {
            log.error(e) { "Cannot write tile $tile: ${e.message}" }
            errors.add(e)
            if (errors.size > 10) {
                errors.removeAt(0)
            }
        }
    }

    override fun close() {
        executor.shutdown()
        writer.close()
        if (errors.isNotEmpty()) {
            throw errors.last()
        }
    }
}

private val threadIdSeed = AtomicInteger()