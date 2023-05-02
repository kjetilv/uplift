package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.nio.file.Path

@Suppress("unused")
class UpliftPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("uplift", UpliftDeployTask::class.java) { task ->
            configureFor(project, task)
        }
        project.tasks.register("uplift-destroy", UpliftDestroyTask::class.java) { task ->
            configureFor(project, task)
        }
    }

    private fun configureFor(project: Project, task: UpliftTask) =
        task.apply {
            account %= "${project.propertyOrNull("account") ?: ""}"
            region %= "${project.propertyOrNull("region") ?: ""}"
            profile %= "${project.propertyOrNull("profile") ?: "default"}"
            stack %= "${project.propertyOrNull("stack") ?: composeName(project)}"
            arch %= resolveArchitecture()
            awsAuth %= resolveAwsAuth()
            jarOutput(project)?.also { jarFile ->
                stackbuilderJar %= jarFile
            }
            stackbuilderClass %= ""
        }


    private fun composeName(project: Project) =
        normalize(
            if (project.name.startsWith(project.group.toString())) project.name
            else "${project.group}-${project.name}"
        )

    private fun normalize(s: String) =
        s.toCharArray().joinToString("") { c -> if (Character.isLetterOrDigit(c)) "$c" else "-" }

    private fun jarOutput(project: Project) =
        project.tasks.findByName("jar")?.outputs?.files?.singleFile?.toPath()

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
