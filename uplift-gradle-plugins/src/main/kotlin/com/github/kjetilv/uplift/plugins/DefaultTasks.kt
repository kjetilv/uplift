package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import java.nio.file.Path

internal fun DefaultTask.docker(cwd: Path, dockerCmd: String, execOperations: ExecOperations) =
    project.resolveProperty("docker.binary", defValue = "docker").let { docker ->
        val cmd = "$docker $dockerCmd"
        execOperations.exec { spec ->
            spec.run {
                this.workingDir = cwd.toFile()
                this.commandLine = cmd.split(" ").also {
                    logger.info("Running command in $cwd")
                    logger.info("  ${it.joinToString(" ")}")
                }
            }.also {
                this.logger.info("Completed: ${cmd}")
            }
        }
    }

private fun Project.resolveProperty(property: String, variable: String? = null, defValue: String? = null) =
    System.getProperty(property)
        ?: variable?.let { System.getenv(it) }
        ?: this.takeIf { it.hasProperty(property) }?.property(property)?.toString()
        ?: defValue
        ?: "$\\{$property}"
