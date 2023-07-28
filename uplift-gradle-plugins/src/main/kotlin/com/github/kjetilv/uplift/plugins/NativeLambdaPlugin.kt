package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI

class NativeLambdaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val target = project.buildDir.toPath().resolve("uplift")
        project.tasks.register("native-lambda", NativeLamdbdaTask::class.java) {
            it.run {
                zipFile.set(target.resolve("${project.name}.zip"))
                bootstrapFile.set(target.resolve(project.name))
                identifier.set(project.name)
                jarTask.set("shadowJar")
                arch.set(System.getProperty("os.arch"))
                jdkVersion.set("17")
                buildsite.set("${project.shortGroupName}-buildsite")
                javaDist.set(
                    URI.create(
                        "https://download.oracle.com/graalvm/20/latest/graalvm-jdk-20_linux-${arch.get()}_bin.tar.gz"
                    )
                )
            }
        }
    }
}
