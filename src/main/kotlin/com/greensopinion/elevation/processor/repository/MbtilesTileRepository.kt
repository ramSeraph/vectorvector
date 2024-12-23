package com.greensopinion.elevation.processor.repository

import com.greensopinion.elevation.processor.TileId
import com.greensopinion.elevation.processor.metrics.MetricsProvider
import com.greensopinion.elevation.processor.util.newThreadFactory
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.sqlite.JDBC
import java.io.File
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


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
        newThreadFactory("Mbtiles-writer")
    ) { r, e ->
        e.queue.put(r)
    }

    private val connection: Connection = createConnection()
    private val insert = prepareInsertStatement()

    override fun store(tile: TileId, extension: String, bytes: ByteArray) {
        executor.submit { write(tile, bytes) }
    }

    private fun write(tile: TileId, bytes: ByteArray) {
        try {
            insert.setInt(1, tile.z)
            insert.setInt(2, tile.x)
            insert.setInt(3, tile.y)
            insert.setBytes(4, bytes)
            insert.execute()
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
        insert.close()
        connection.close()
        if (errors.isNotEmpty()) {
            throw errors.last()
        }
    }

    private fun createConnection(): Connection {
        outputFile.delete()
        outputFile.parentFile.mkdirs()
        return JDBC.createConnection("jdbc:sqlite:${outputFile.absolutePath}", Properties()).also {
            initialize(it)
        }
    }

    private fun prepareInsertStatement(): PreparedStatement =
        connection.prepareStatement("INSERT INTO tiles (zoom_level, tile_column, tile_row, tile_data) VALUES(?,?,?,?)")


    private fun initialize(connection: Connection) {
        connection.executeStatement("CREATE TABLE metadata (name text,value text);")
        connection.executeStatement("CREATE UNIQUE INDEX name on metadata (name);")
        connection.executeStatement("CREATE TABLE tiles (zoom_level integer, tile_column integer, tile_row integer, tile_data blob);")
        connection.executeStatement("CREATE UNIQUE INDEX tile_index on tiles (zoom_level, tile_column, tile_row);")
    }
}

private fun Connection.executeStatement(sql: String) {
    createStatement().use { statement ->
        statement.execute(sql)
    }
}