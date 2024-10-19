package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI

@Suppress("unused")
class NativeLambdaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.register<NativeLamdbdaTask>("native-lambda") {

            val projectName = project.name
            val target = project.buildSubDirectory("uplift")
            val osArch = System.getProperty("os.arch")
            val javaDistUri = URI.create(
                "https://download.oracle.com/graalvm/23/latest/graalvm-jdk-23_linux-${osArch}_bin.tar.gz"
            )

            classPath %= project.classpath
            zipFile %= target.resolve("$projectName.zip")
            identifier %= projectName
            bootstrapFile %= target.resolve(projectName)
            arch %= osArch
            buildsite %= "${project.shortGroupName}-buildsite"
            javaDist %= javaDistUri

            dependsOn("jar")
        }
    }
}
