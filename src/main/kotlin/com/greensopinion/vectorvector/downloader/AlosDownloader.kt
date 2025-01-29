package com.greensopinion.vectorvector.downloader

import com.greensopinion.vectorvector.cli.CliOptions
import com.greensopinion.vectorvector.util.unzip
import kotlinx.coroutines.runBlocking
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.io.File

fun main(args: Array<String>) {
    val options = parseCommandLine(args)
    if (options.help) {
        CommandLine.usage(CliOptions(), System.out)
        return
    }
    val dataDir = options.dataDir!!
    dataDir.mkdirs()
    require(dataDir.exists() && dataDir.isDirectory) { "${options.dataDir} must be a directory" }
    val files = AlosFilenames().generate()
    val toRetrieve = files.map { "${options.baseUrl}$it" }.toList()
    @Suppress("DeferredResultUnused")
    runBlocking {
        downloadWithConcurrency(toRetrieve, dataDir, maxConcurrency = options.concurrency) {
            unzip(it)
        }
    }
}


@Command
class DownloaderCliOptions {

    @Option(names = ["-h", "--help"], usageHelp = true, description = ["display this help message"])
    var help = false

    @Option(
        names = ["-d", "--data"],
        required = true,
        description = ["The data directory containing elevation data in GeoTIFF format."]
    )
    var dataDir: File? = null


    @Option(
        names = ["-s", "--source"],
        description = ["The source URL."]
    )
    var baseUrl: String = "https://www.eorc.jaxa.jp/ALOS/aw3d30/data/release_v2404/"

    @Option(
        names = ["-c", "--concurrency"],
        description = ["The maximum number of concurrent downloads."]
    )
    var concurrency: Int = 5
}

private fun parseCommandLine(args: Array<String>) = DownloaderCliOptions().also {
    CommandLine(it).parseArgs(*args)
}
