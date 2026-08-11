package com.github.kjetilv.uplift.plugins

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.COPY_ATTRIBUTES
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.time.Instant
import kotlin.io.path.isDirectory


internal val Path.isZip get() = fileName.toString().endsWith(".zip")

internal val Path.isJar get() = fileName.toString().endsWith(".jar")

internal val Path.isDir get() = fileName.isDirectory()

internal val Path.isActualDirectory get() = Files.isDirectory(this)

internal fun copyTo(sourcePath: Path, context: Path, target: String? = null) =
    context.resolve(target?.let { Path.of(it) } ?: sourcePath.fileName)
        .also { targetPath ->
            if (shouldCopy(sourcePath, targetPath)) {
                Files.copy(sourcePath, targetPath, REPLACE_EXISTING, COPY_ATTRIBUTES)
            }
        }

private fun shouldCopy(source: Path, target: Path) =
    !Files.exists(target) || Files.size(source) != Files.size(target) || modified(source) > modified(target)

private fun modified(source: Path): Instant = Files.getLastModifiedTime(source).toInstant()
