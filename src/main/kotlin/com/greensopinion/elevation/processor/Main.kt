package com.greensopinion.elevation.processor

import com.greensopinion.elevation.processor.sink.LogTileSink
import picocli.CommandLine

fun main(args: Array<String>) {
    val options = parseCommandLine(args)

    Processor(
        tileRange = options.toTileRange(),
        sink = LogTileSink()
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