package com.greensopinion.vectorvector.elevation

import com.greensopinion.vectorvector.Coordinates
import com.greensopinion.vectorvector.ElevationTile
import com.greensopinion.vectorvector.EmptyTile
import com.greensopinion.vectorvector.metrics.MetricsProvider
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File


class Carto30ElevationDataStore(
    private val dataFolder: File,
    private val metricsProvider: MetricsProvider,
    private val log: KLogger = KotlinLogging.logger {}
) : BlockStore {
    private val blockSize = Degrees(1.0)

    override val blockExtent: Int = 3600
    private val emptyTile = EmptyTile(blockExtent)
    private val mapper = DegreeBlockMapper(blockSize = blockSize, blockExtent = blockExtent)

    override fun load(blockId: BlockId): ElevationTile {
        metricsProvider.get().addCount("Carto30ElevationDataStore.load")

        val bottomLeft = mapper.reverseMap(blockId)
        val blockFile = toCartoFile(dataFolder, bottomLeft)
        return if (blockFile.exists()) {
            load(blockFile)
        } else {
            emptyTile
        }
    }

    override fun provides(blockId: BlockId): Boolean {
        val bottomLeft = mapper.reverseMap(blockId)
        val blockFile = toCartoFile(dataFolder, bottomLeft)
        return blockFile.exists()
    }

    private fun toCartoFile(folder: File, bottomLeft: Coordinates) =
        File(folder, bottomLeft.toCarto30Filename())

    private fun load(file: File) = if (file.exists()) {
        RasterTileReader(nodataVals=intArrayOf(-255), elevationOffset=83.0).read(file)
    } else emptyTile
}
