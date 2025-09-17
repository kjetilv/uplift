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
                "https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-25.0.0/graalvm-community-jdk-25.0.0_linux-${osArch}_bin.tar.gz"
            )

            classPath %= project.classpath.also { files ->
                logger.info("$this: Classpath: ${files.joinToString(":")}")
            }
            zipFile %= target.resolve("$projectName.zip").also { files ->
                logger.info("$this: Zipfile: ${files.joinToString(":")}")
            }
            identifier %= projectName
            bootstrapFile %= target.resolve(projectName)
            arch %= osArch
            buildsite %= "${project.shortGroupName}-buildsite"
            javaDist %= javaDistUri

            enablePreview.convention(false)
            addModules.convention("")
            otherOptions.convention("")

            dependsOn("jar")
        }
    }
}
