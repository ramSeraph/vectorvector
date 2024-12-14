package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.elevation.*
import com.greensopinion.elevation.processor.sink.TerrariumSink
import picocli.CommandLine

fun main(args: Array<String>) {
    val options = parseCommandLine(args)
    val tileExtent = 256
    Processor(
        tileRange = options.toTileRange(),
        sink = TerrariumSink(
            elevationDataStore = BlockElevationDataStore(
                blockSize = Degrees(5.0),
                blockExtent = 6000,
                tileExtent = tileExtent,
                blockStore = CachingBlockStore(FilesystemBlockStore(
                    folder = options.dataDir!!
                ))
            ),
            extent = tileExtent,
            outputFolder = options.outputDir!!
        )
    ).process()
}

private fun parseCommandLine(args: Array<String>): CliOptions {
    val options = CliOptions();
    CommandLine(options).parseArgs(*args)
    return options
}

private fun CliOptions.toTileRange() = TileRange(
    minZ = this.minZ,
    maxZ = this.maxZ,
    minX = this.minX,
    maxX = this.maxX,
    minY = this.minY,
    maxY = this.maxY,
)