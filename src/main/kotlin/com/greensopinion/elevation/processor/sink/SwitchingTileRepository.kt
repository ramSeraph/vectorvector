package com.greensopinion.elevation.processor.sink

import com.greensopinion.elevation.processor.TileId
import java.io.Closeable

class SwitchingTileRepository(
    private val extensionToRepository: Map<String, TileRepository>
) : TileRepository {

    override fun store(tile: TileId, extension: String, bytes: ByteArray) {
        val repository = extensionToRepository[extension] ?: throw Exception("Unexpected extension: $extension")
        repository.store(tile, extension, bytes)
    }

    override fun close() {
        closeSafely(extensionToRepository.values)
    }
}

private fun closeSafely(closeables: Collection<Closeable>) {
    val exceptions = mutableListOf<Exception>()
    closeables.forEach {
        try {
            it.close()
        } catch (e: Exception) {
            exceptions.add(e)
        }
    }
    if (exceptions.isNotEmpty()) {
        val first = exceptions.removeFirst()
        exceptions.forEach {
            first.addSuppressed(it)
        }
        throw first
    }
}