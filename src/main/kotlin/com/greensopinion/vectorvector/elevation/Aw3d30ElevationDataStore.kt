package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.EmptyTile
import com.greensopinion.vectorvector.metrics.MetricsProvider
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File


class Aw3d30ElevationDataStore(
    private val dataFolder: File,
    private val outputFolder: File,
    private val metricsProvider: MetricsProvider,
    private val log: KLogger = KotlinLogging.logger {}
) : BlockStore {
    private val unzipLock = Any()

    private val blockSize = Degrees(1.0)

    override val blockExtent: Int = 3600
    private val emptyTile = EmptyTile(blockExtent)
    private val mapper = DegreeBlockMapper(blockSize = blockSize, blockExtent = blockExtent)

    override fun load(blockId: BlockId): ElevationTile {
        metricsProvider.get().addCount("Aw3d30ElevationDataStore.load")

        val bottomLeft = mapper.reverseMap(blockId)
        val blockFolder = File(outputFolder, bottomLeft.to5x5Filename())
        return if (blockFolder.exists()) {
            load(to1x1File(blockFolder, bottomLeft))
        } else {
            emptyTile
        }
    }

    override fun provides(blockId: BlockId): Boolean {
        val bottomLeft = mapper.reverseMap(blockId)
        val blockFolder = File(outputFolder, bottomLeft.to5x5Filename())
        return blockFolder.exists() && to1x1File(blockFolder, bottomLeft).exists()
    }

    private fun to1x1File(folder: File, bottomLeft: Coordinates) =
        File(folder, "ALPSMLC30_${bottomLeft.to1x1Filename()}_DSM.tif")

    private fun load(file: File) = if (file.exists()) {
        RasterTileReader().read(file)
    } else emptyTile
}
