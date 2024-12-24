package com.greensopinion.vectorvector.repository

import com.greensopinion.vectorvector.TileId
import com.greensopinion.vectorvector.metrics.MetricsProvider
import com.greensopinion.vectorvector.util.newThreadFactory
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.sqlite.JDBC
import java.io.File
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.Properties
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty1

/**
 * Metadata per the [spec](https://github.com/mapbox/mbtiles-spec/blob/master/1.3/spec.md)
 */
class MbtilesMetadata(
    val name: String,
    val format: String,
    val minZoom: Int,
    val maxZoom: Int,
    val type: String = "overlay",
    val json: String,
    val bounds: String = "-180.0,-85,180,85"
)

class MbtilesTileRepository(
    private val outputFile: File,
    private val metricsProvider: MetricsProvider,
    private val truncate: Boolean = true,
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

    fun setMetadata(metadata: MbtilesMetadata) {
        insertMetadata(metadata, MbtilesMetadata::name)
        insertMetadata(metadata, MbtilesMetadata::format)
        insertMetadata(metadata, MbtilesMetadata::minZoom)
        insertMetadata(metadata, MbtilesMetadata::maxZoom)
        insertMetadata(metadata, MbtilesMetadata::type)
        insertMetadata(metadata, MbtilesMetadata::json) { it.trim().isNotEmpty() }
        insertMetadata(metadata, MbtilesMetadata::bounds)
    }

    private fun <V> insertMetadata(
        metadata: MbtilesMetadata,
        property: KProperty1<MbtilesMetadata, V>,
        predicate: (V) -> Boolean = { true }
    ) {
        val value = property.get(metadata)
        if (predicate(value)) {
            val statement =
                connection.prepareStatement("INSERT INTO metadata (name,value) VALUES (?,?) ON CONFLICT(name) DO UPDATE SET value=excluded.value;")
            statement.use {
                it.setString(1, property.name)
                it.setString(2, value.toString())
                if (it.executeUpdate() != 1) {
                    throw Exception("unexpected result for ${property.name}:${property.get(metadata)}")
                }
            }
        }
    }

    override fun store(tile: TileId, extension: String, bytes: ByteArray) {
        executor.submit { write(tile, bytes) }
    }

    fun read(tile: TileId): Future<ByteArray> {
        val result = CompletableFuture<ByteArray>()
        executor.submit {
            try {
                result.complete(readBytes(tile))
            } catch (e: Exception) {
                result.completeExceptionally(e)
            }
        }
        return result
    }

    private fun readBytes(tile: TileId): ByteArray {
        val statement =
            connection.prepareStatement("SELECT tile_data FROM tiles WHERE zoom_level = ? AND tile_column = ? AND tile_row = ?")
        statement.use {
            it.setInt(1, tile.z)
            it.setInt(2, tile.x)
            it.setInt(3, tile.y.toTms(tile.z))
            val results = it.executeQuery()
            results.use { r ->
                if (r.next()) {
                    return r.getBinaryStream(1).readAllBytes()
                }
                throw Exception("not found: $tile")
            }
        }
    }

    private fun write(tile: TileId, bytes: ByteArray) {
        try {
            insert.setInt(1, tile.z)
            insert.setInt(2, tile.x)
            insert.setInt(3, tile.y.toTms(tile.z))
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
        executor.awaitTermination(10, TimeUnit.MINUTES)
        insert.close()
        connection.close()
        if (errors.isNotEmpty()) {
            throw errors.last()
        }
    }

    private fun createConnection(): Connection {
        outputFile.parentFile.mkdirs()
        val exists = outputFile.exists()
        if (exists && truncate) {
            outputFile.delete()
        }
        return JDBC.createConnection("jdbc:sqlite:${outputFile.absolutePath}", Properties()).also {
            initialize(it)
        }
    }

    private fun prepareInsertStatement(): PreparedStatement =
        connection.prepareStatement("INSERT INTO tiles (zoom_level,tile_column,tile_row,tile_data) VALUES (?,?,?,?)")


    private fun initialize(connection: Connection) {
        if (!tableExists(connection, "metadata")) {
            connection.executeStatement("CREATE TABLE metadata (name text,value text);")
            connection.executeStatement("CREATE UNIQUE INDEX name on metadata (name);")
        }
        if (!tableExists(connection, "tiles")) {
            connection.executeStatement("CREATE TABLE tiles (zoom_level integer, tile_column integer, tile_row integer, tile_data blob);")
            connection.executeStatement("CREATE UNIQUE INDEX tile_index on tiles (zoom_level, tile_column, tile_row);")
        }
    }

    private fun tableExists(connection: Connection, name: String): Boolean =
        connection.createStatement().use {
            it.executeQuery("SELECT name FROM sqlite_master WHERE type = 'table' AND name = '$name';").use { result ->
                result.next()
            }
        }

}

private fun Connection.executeStatement(sql: String) {
    createStatement().use { statement ->
        statement.execute(sql)
    }
}

private fun Int.toTms(z: Int) = (1 shl z) - 1 - this