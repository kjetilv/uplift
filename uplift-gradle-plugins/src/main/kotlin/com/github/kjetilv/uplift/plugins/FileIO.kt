package com.github.kjetilv.uplift.plugins

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant


internal val Path.isZip get() = fileName.toString().endsWith(".zip")

internal val Path.isJar get() = fileName.toString().endsWith(".jar")

internal val Path.isActualFile get() = Files.isRegularFile(this)

internal val Path.isActualDirectory get() = Files.isDirectory(this)

internal fun copyTo(sourcePath: Path, context: Path, target: String? = null) =
    context.resolve(target?.let { Path.of(it) } ?: sourcePath.fileName)
        .also { targetPath ->
            if (shouldCopy(sourcePath, targetPath)) {
                Files.copy(
                    sourcePath, targetPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
                )
            }
        }

private fun shouldCopy(source: Path, target: Path) =
    !Files.exists(target) || Files.size(source) != Files.size(target) || modified(source) > modified(target)

private fun modified(source: Path): Instant = Files.getLastModifiedTime(source).toInstant()
