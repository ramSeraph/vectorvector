package com.greensopinion.vectorvector.cli

import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File

@Command(name = "java --jar vectorvector-cli.jar")
class CliOptions {
    @Option(
        names = ["-d", "--data"],
        required = true,
        description = ["The data directory containing elevation data in GeoTIFF format."]
    )
    var dataDir: File? = null

    @Option(
        names = ["-o", "--output"],
        required = true,
        description = ["Specifies the directory where the generated output files will be saved. Defaults to \${DEFAULT-VALUE}"]
    )
    var outputDir: File = File(".")

    @Option(
        names = ["--validate"],
        description = [" Validates the elevation data can be read before generating tiles. The command exits without generating output if validation fails. Defaults to \${DEFAULT-VALUE}"]
    )
    var validateData: Boolean = false

    @Option(
        names = ["--terrarium"],
        description = ["Generates Terrarium-style raster tiles, commonly used for rendering 3D-like terrain features. Defaults to \${DEFAULT-VALUE}"]
    )
    var terrarium: Boolean = false

    @Option(
        names = ["--vector"],
        description = ["Generates vector tiles with contour lines. Defaults to \${DEFAULT-VALUE}"]
    )
    var vector: Boolean = true

    @Option(
        names = ["--hillshadeRaster"],
        description = ["Generates hillshade raster tiles, which simulate the shading effects of the sun on the terrain to enhance visual representation of elevation data. Defaults to \${DEFAULT-VALUE}"]
    )
    var hillshadeRaster: Boolean = true

    @Option(
        names = ["-f", "--outputFormat"],
        required = true,
        description = ["The format of the output, must be one of \${COMPLETION-CANDIDATES}"]
    )
    var outputFormat: CliOutputFormat? = null

    @Option(
        names = ["-a", "--area"],
        description = ["The name of an area, which specifies a bounds for tile generation (minZ, maxZ, minX, maxX, minY, maxY). If specified, the area supersedes all Z/X/Y options. Must be one of \${COMPLETION-CANDIDATES}"]
    )
    var area: NamedArea? = null

    @Option(
        names = ["-minZ"],
        description = ["The minimum zoom level for which tiles are generated. Defaults to \${DEFAULT-VALUE}"]
    )
    var minZ: Int = NamedArea.world.bounds.minZ

    @Option(
        names = ["-maxZ"],
        description = ["The maximum zoom level for which tiles are generated. Defaults to \${DEFAULT-VALUE}"]
    )
    var maxZ: Int = NamedArea.world.bounds.maxZ

    @Option(
        names = ["-minX"],
        description = ["Specifies the minimum longitude (X coordinate) for the tile area at the minZ zoom level. Defaults to \${DEFAULT-VALUE}"]
    )
    var minX: Int = NamedArea.world.bounds.maxX

    @Option(
        names = ["-maxX"],
        description = ["Specifies the maximum longitude (X coordinate) for the tile area at the minZ zoom level. Defaults to \${DEFAULT-VALUE}"]
    )
    var maxX: Int = NamedArea.world.bounds.maxX

    @Option(
        names = ["-minY"],
        description = ["Specifies the minimum latitude (Y coordinate) for the tile area at the minZ zoom level. Defaults to \${DEFAULT-VALUE}"]
    )
    var minY: Int = NamedArea.world.bounds.minY

    @Option(
        names = ["-maxY"],
        description = ["Specifies the maximum latitude (Y coordinate) for the tile area at the minZ zoom level. Defaults to \${DEFAULT-VALUE}"]
    )
    var maxY: Int = NamedArea.world.bounds.maxY

    @Option(names = ["-h", "--help"], usageHelp = true, description = ["display this help message"])
    var help = false
}

enum class CliOutputFormat {
    files,
    mbtiles
}

enum class NamedArea(val bounds: AreaBounds) {
    wholeworld(AreaBounds(minZ = 6, maxZ = 12, minX = 0, maxX = 63, minY = 0, maxY = 63)),
    world(AreaBounds(minZ = 6, maxZ = 12, minX = 0, maxX = 63, minY = 13, maxY = 43)),
    northamerica(AreaBounds(minZ = 6, maxZ = 13, minX = 11, maxX = 23, minY = 5, maxY = 22)),
    southamerica(AreaBounds(minZ = 6, maxZ = 12, minX = 17, maxX = 25, minY = 21, maxY = 43)),
    europe(AreaBounds(minZ = 6, maxZ = 12, minX = 23, maxX = 25, minY = 5, maxY = 15)),
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