package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import java.nio.file.Path

internal fun DefaultTask.docker(cwd: Path, dockerCmd: String) =
    project.resolveProperty("docker.binary", defValue = "docker").let { docker ->
        exe(cwd, "$docker $dockerCmd")
    }

internal fun DefaultTask.exe(cwd: Path, cmd: String) =
    project.exec { spec ->
        spec.run {
            workingDir = cwd.toFile()
            commandLine = cmd.toCommand().also {
                logger.info("Running command in $cwd")
                logger.info("  ${it.joinToString(" ")}")
            }
        }.also {
            logger.info("Completed: $cmd")
        }
    }

private fun String.toCommand() = this.split(" ")

private fun Project.resolveProperty(property: String, variable: String? = null, defValue: String? = null) =
    System.getProperty(property)
        ?: variable?.let { System.getenv(it) }
        ?: this.takeIf { it.hasProperty(property) }?.property(property)?.toString()
        ?: defValue
        ?: "$\\{$property}"
