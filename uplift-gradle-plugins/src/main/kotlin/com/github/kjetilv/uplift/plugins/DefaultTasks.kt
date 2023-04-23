package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import java.nio.file.Path

internal fun DefaultTask.exe(cwd: Path, command: String) {
    project.exec {
        it.run {
            workingDir = cwd.toFile()
            commandLine = command.toCommand()
        }
    }
}

private fun String.toCommand() = this.split(" ")
