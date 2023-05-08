package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI

@Suppress("unused")
class NativeLambdaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val target = project.buildDir.toPath().resolve("uplift")
        project.tasks.register("native-lambda", NativeLamdbdaTask::class.java) {
            it.run {
                zipFile.set(target.resolve("${project.name}.zip"))
                identifier.set(project.name)
                jarTask.set("shadowJar")
                arch.set(System.getProperty("os.arch"))
                jdkVersion.set("17")
                graalVersion.set("22.3.1")
                buildsite.set("${project.shortGroupName}-buildsite")
                javaDist.set(
                    URI.create(
                        "https://github.com/graalvm/graalvm-ce-builds/releases/download" +
                                "/vm-${graalVersion.get()}" +
                                "/graalvm-ce-java${jdkVersion.get()}-linux-${arch.get()}-${graalVersion.get()}" +
                                ".tar.gz"
                    )
                )
                javaHome.set("graalvm-ce-java${jdkVersion.get()}-${graalVersion.get()}")
            }
        }
    }
}
