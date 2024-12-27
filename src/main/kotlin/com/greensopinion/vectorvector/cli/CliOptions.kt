package com.greensopinion.vectorvector.cli

import picocli.CommandLine.Option
import java.io.File

class CliOptions {
    @Option(
        names = ["-d", "--data"],
        required = true,
        description = ["The data directory containing elevation data in tiff format."]
    )
    var dataDir: File? = null

    @Option(names = ["-o", "--output"], required = true, description = ["The directory containing output files."])
    var outputDir: File = File(".")

    @Option(names = ["--validate"])
    var validateData: Boolean = false

    @Option(names = ["--terrarium"])
    var terrarium: Boolean = false

    @Option(names = ["--vector"])
    var vector: Boolean = true

    @Option(names = ["--hillshadeRaster"])
    var hillshadeRaster: Boolean = true

    @Option(
        names = ["-f", "--outputFormat"],
        required = true,
        description = ["The format of the output, must be one of \${COMPLETION-CANDIDATES}"]
    )
    var outputFormat: CliOutputFormat? = null

    @Option(
        names = ["--area"],
        description = ["The name of an area, which specifies a bounds. Must be one of \${COMPLETION-CANDIDATES}"]
    )
    var area: NamedArea? = null

    @Option(names = ["-minZ"])
    var minZ: Int = NamedArea.world.bounds.minZ

    @Option(names = ["-maxZ"])
    var maxZ: Int = NamedArea.world.bounds.maxZ

    @Option(names = ["-minX"])
    var minX: Int = NamedArea.world.bounds.maxX

    @Option(names = ["-maxX"])
    var maxX: Int = NamedArea.world.bounds.maxX

    @Option(names = ["-minY"])
    var minY: Int = NamedArea.world.bounds.minY

    @Option(names = ["-maxY"])
    var maxY: Int = NamedArea.world.bounds.maxY
}

enum class CliOutputFormat {
    files,
    mbtiles
}

enum class NamedArea(val bounds: AreaBounds) {
    wholeworld(AreaBounds(minZ = 6, maxZ = 12, minX = 0, maxX = 63, minY = 0, maxY = 63)),
    world(AreaBounds(minZ = 6, maxZ = 12, minX = 0, maxX = 63, minY = 13, maxY = 43)),
    vancouver(AreaBounds(z = 6, x = 10, y = 21, maxZ = 12)),
    deepcove(AreaBounds(z = 11, x = 324, y = 700, maxZ = 12)),
    pnw(AreaBounds(minZ = 6, maxZ = 12, minX = 9, maxX = 10, minY = 21, maxY = 22)),
    sanfrancisco(AreaBounds(z = 6, x = 10, y = 24, maxZ = 12)),
    newyork(AreaBounds(z = 6, x = 18, y = 24, maxZ = 12)),
    paris(AreaBounds(z = 6, x = 32, y = 22, maxZ = 12)),
    rome(AreaBounds(z = 6, x = 34, y = 23, maxZ = 12)),
    tokyo(AreaBounds(z = 6, x = 56, y = 25, maxZ = 12)),
    palma(AreaBounds(z = 6, x = 32, y = 24, maxZ = 12))
}

class AreaBounds(
    val minZ: Int,
    val maxZ: Int,
    val minX: Int,
    val maxX: Int,
    val minY: Int,
    val maxY: Int
) {
    constructor(maxZ: Int, z: Int, x: Int, y: Int) : this(minZ = z, maxZ = maxZ, minX = x, maxX = x, minY = y, maxY = y)

    fun applyTo(options: CliOptions) {
        options.minZ = minZ
        options.maxZ = maxZ
        options.minX = minX
        options.maxX = maxX
        options.minY = minY
        options.maxY = maxY
    }
}