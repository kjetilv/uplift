package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI

class NativeLambdaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("native-lambda", NativeLamdbdaTask::class.java) {
            it.apply {

                val target = project.buildSubDirectory("uplift")
                val osArch = System.getProperty("os.arch")
                val javaDistUri = osArch.asGraalUri()
                val projectName = project.name

                classPath.set(project.classpath)
                zipFile.set(target.resolve("$projectName.zip"))
                identifier.set(projectName)
                jarTask.set("shadowJar")
                bootstrapFile.set(target.resolve(projectName))
                arch.set(osArch)
                buildsite.set("${project.shortGroupName}-buildsite")
                javaDist.set(javaDistUri)

                dependsOn("jar")

            }
        }
    }

    private fun String?.asGraalUri(): URI? =
        URI.create("https://download.oracle.com/graalvm/21/latest/graalvm-jdk-21_linux-${this}_bin.tar.gz")
}
