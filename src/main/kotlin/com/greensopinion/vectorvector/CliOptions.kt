package com.greensopinion.vectorvector

import picocli.CommandLine.Option
import java.io.File

class CliOptions {
    @Option(names = ["-d", "--data"], required = true, description = ["The data directory containing elevation data in tiff format."])
    var dataDir: File? = null

    @Option(names = ["-o", "--output"], required = true, description = ["The directory containing output files."])
    var outputDir: File? = null

    @Option(names=["--validate"],)
    var validateData: Boolean = false

    @Option(names=["--terrarium"])
    var terrarium: Boolean = false

    @Option(names=["--vector"])
    var vector: Boolean = true

    @Option(names = ["-f", "--outputFormat"], required = true, description = ["The format of the output, must be one of \${COMPLETION-CANDIDATES}"])
    var outputFormat: CliOutputFormat? = null

    @Option(names = ["-minZ"])
    var minZ: Int = 6
    @Option(names = ["-maxZ"])
    var maxZ: Int = 12

    @Option(names = ["-minX"])
    var minX: Int = 10
    @Option(names = ["-maxX"])
    var maxX: Int = 10

    @Option(names = ["-minY"])
    var minY: Int = 21
    @Option(names = ["-maxY"])
    var maxY: Int = 21
}

enum class CliOutputFormat {
    files,
    mbtiles
}