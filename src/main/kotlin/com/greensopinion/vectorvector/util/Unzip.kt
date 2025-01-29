package com.greensopinion.vectorvector.util

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.EOFException
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream

private val log: KLogger = KotlinLogging.logger { }

fun unzip(sourceFile: File) {
    try {
        unzipUnsafe(sourceFile)
    } catch (e: EOFException) {
        throw Exception("file ${sourceFile.absolutePath} size ${sourceFile.length()}", e)
    }
}

private fun unzipUnsafe(sourceFile: File) {
    val targetFolderName = sourceFile.name.split(".").dropLast(1).joinToString(".")
    val parentFolder = sourceFile.parentFile
    val targetFolder = File(parentFolder, targetFolderName)
    if (!targetFolder.exists()) {
        log.info { "Unzipping $sourceFile" }
        val tmpFolder = File(targetFolder.parentFile, "${targetFolder.name}-tmp")
        tmpFolder.mkdirs()
        ZipInputStream(sourceFile.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val entryName = entry.name
                require(entryName.startsWith(targetFolderName)) { "expected zip path $entryName to start with $targetFolderName" }
                val localName = entryName.split("/").drop(1).joinToString("/")
                if (localName.isNotEmpty()) {
                    val entryFile = File(tmpFolder, localName)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile.mkdirs()
                        Files.copy(zip, entryFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
                entry = zip.nextEntry
            }
        }
        Files.move(tmpFolder.toPath(), targetFolder.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}