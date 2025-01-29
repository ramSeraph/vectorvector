package com.greensopinion.vectorvector.downloader

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.io.File

val log = KotlinLogging.logger { }

fun CoroutineScope.downloadWithConcurrency(
    urls: List<String>,
    outputDir: File,
    maxConcurrency: Int,
    consumer: (File) -> Unit
) = async {
    val semaphore = Semaphore(maxConcurrency)

    val jobs = urls.map { url ->
        launch {
            val filename = url.split("/").last()
            val file = File(outputDir, filename)
            if (!file.exists()) {
                semaphore.acquire()
                try {
                    downloadUrl(url, file)
                } finally {
                    semaphore.release()
                }
            }
            if (file.exists()) {
                consumer(file)
            }
        }
    }
    jobs.joinAll()
}


private suspend fun downloadUrl(url: String, file: File): Boolean {
    return try {
        log.info { "downloading $url" }
        val command = listOf("wget", "-q", "-O", file.absolutePath, url)
        val exitCode = withContext(Dispatchers.IO) {
            ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start().waitFor()
        }
        exitCode == 0
    } catch (e: Exception) {
        log.error(e) { "Failed to download $url" }
        false
    }
}
