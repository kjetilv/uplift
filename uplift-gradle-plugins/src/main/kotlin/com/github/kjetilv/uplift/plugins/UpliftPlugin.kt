package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.nio.file.Path

@Suppress("unused")
class UpliftPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("uplift", UpliftTask::class.java) { task ->
            task.run {
                arch %= resolveArchitecture()
                profile %= "default"
                awsAuth %= resolveAwsAuth()
                project.tasks.findByName("jar")?.outputs?.files?.singleFile?.toPath()
                    ?.also { jarFile ->
                        stackbuilderJar %= jarFile
                    } ?: throw IllegalStateException("No output from jar task was found")
                stackbuilderClass %= ""
            }
        }
        project.tasks.register("uplift-destroy", UpliftDestroyTask::class.java)
    }

    private fun resolveAwsAuth() =
        Path.of(System.getProperty("user.home")).resolve(".aws").toAbsolutePath().toString()

    private fun resolveArchitecture() = System.getProperty("os.arch").let {
        when (it) {
            "aarch64" -> "arm64v8"
            "x86_64" -> "amd64"
            else -> it
        }
    }
}
