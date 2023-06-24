package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files.createDirectories
import java.nio.file.Files.isDirectory
import java.nio.file.Path
import kotlin.io.path.Path

private const val lambdaName = "bootstrap"

@Suppress("unused")
open class LambdaTask : DefaultTask() {

    @Input
    lateinit var artifact: String

    @Input
    lateinit var target: String

    @TaskAction
    fun perform(): Path =
        zipFile(Path(artifact), zipArtifact).also {
            logger.info("Wrote native artifact $path to $zipArtifact as $lambdaName")
        }

    private val zipArtifact: Path get() = resolve(Path(target)).also { createDirectories(it.parent) }

    private fun resolve(it: Path): Path = when {
        isDirectory(it) ->
            it.resolve("$artifact.zip")

        it.toString().endsWith(".zip") ->
            it

        else ->
            it.parent.resolve("${it.fileName}.zip")
    }
}
