package com.github.kjetilv.uplift.plugins

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal fun zipFile(source: Path, zipFile: Path? = null) =
    (zipFile ?: Path.of("$source.zip")).also { zipPath ->
        ZipOutputStream(Files.newOutputStream(zipPath))
            .use { stream ->
                stream.run {
                    putNextEntry(ZipEntry("bootstrap"))
                    Files.newInputStream(source).use { fileIn ->
                        fileIn.copyTo(this)
                    }
                    closeEntry()
                }
            }
    }
