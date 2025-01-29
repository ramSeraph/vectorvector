package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.EmptyTile
import com.greensopinion.vectorvector.metrics.MetricsProvider
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream


class SrtmBlockStore(
    private val folder: File,
    private val metricsProvider: MetricsProvider,
    private val log: KLogger = KotlinLogging.logger {}
) : BlockStore {
    override val blockExtent: Int = 6000
    private val emptyTile = EmptyTile(blockExtent)

    override fun load(blockId: BlockId): ElevationTile {
        if (blockId.x < 0 || blockId.y < 0) {
            return emptyTile
        }
        metricsProvider.get().addCount("FilesystemBlockStore.load")
        val file = prepareArea(blockId)
        return if (file.exists()) load(file) else emptyTile
    }

    private fun prepareArea(blockId: BlockId): File {
        val name = "srtm_${blockId.x.keyString()}_${blockId.y.keyString()}"
        val file = File(folder, "$name.tif")
        if (!file.exists()) {
            val zip = File(folder, "$name.zip")
            if (zip.exists()) {
                unzip(folder, zip)
            }
        }
        return file
    }

    override fun provides(blockId: BlockId): Boolean = prepareArea(blockId).exists()

    private fun unzip(folder: File, sourceFile: File) {
        ZipInputStream(sourceFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val localName = entry.name
                if (!entry.isDirectory && localName.endsWith(".tif")) {
                    val entryFile = File(folder, localName)
                    Files.copy(zip, entryFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
                entry = zip.nextEntry
            }
        }
    }

    private fun load(file: File): ElevationTile = RasterTileReader(log).read(file)
}

private fun Int.keyString() = this.toString().padStart(2, '0')