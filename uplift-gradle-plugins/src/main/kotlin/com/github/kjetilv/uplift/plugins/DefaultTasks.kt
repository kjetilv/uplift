package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import java.nio.file.Path

internal fun DefaultTask.docker(cwd: Path, dockerCmd: String) = exe(cwd, "docker $dockerCmd")

internal fun DefaultTask.exe(cwd: Path, cmd: String) =
    project.exec { spec ->
        spec.run {
            workingDir = cwd.toFile()
            commandLine = cmd.also {
                logger.info("Running command in $cwd")
                logger.info("  $it")
            }.toCommand()
        }.also {
            logger.info("Completed: $cmd")
        }
    }

private fun String.toCommand() = this.split(" ")
