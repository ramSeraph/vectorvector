package com.greensopinion.elevation.processor

import picocli.CommandLine.Option
import java.io.File

class CliOptions {
    @Option(names = ["-d", "--data"], required = true, description = ["The data directory containing elevation data in tiff format."])
    var dataDir: File? = null

    @Option(names = ["-minZ"])
    var minZ: Int = 12
    @Option(names = ["-minX"])
    var minX: Int = 646
    @Option(names = ["-minY"])
    var minY: Int = 1401
    @Option(names = ["-maxZ"])
    var maxZ: Int = 12
    @Option(names = ["-maxX"])
    var maxX: Int = 646
    @Option(names = ["-maxY"])
    var maxY: Int = 1401
}