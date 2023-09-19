package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI

class NativeLambdaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("native-lambda", NativeLamdbdaTask::class.java) {
            val target = project.buildSubDirectory("uplift")
            val arch = System.getProperty("os.arch")
            val projectName = project.name

            it.zipFile.set(target.resolve("$projectName.zip"))
            it.identifier.set(projectName)
            it.jarTask.set("shadowJar")
            it.jdkVersion.set("17")
            it.bootstrapFile.set(target.resolve(projectName))
            it.arch.set(arch)
            it.buildsite.set("${project.shortGroupName}-buildsite")
            it.javaDist.set(arch.asGraalUri())
        }
    }

    private fun String?.asGraalUri(): URI? = URI.create("https://download.oracle.com/graalvm/21/latest/graalvm-jdk-21_linux-${this}_bin.tar.gz")
}
