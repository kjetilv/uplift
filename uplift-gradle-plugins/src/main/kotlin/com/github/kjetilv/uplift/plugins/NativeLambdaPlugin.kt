package com.github.kjetilv.uplift.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI

@Suppress("unused")
class NativeLambdaPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("native-lambda", NativeLamdbdaTask::class.java) {
            it.apply {
                val projectName = project.name
                val target = project.buildSubDirectory("uplift")
                val osArch = "os.arch".systemProperty
                val javaDistUri =
                    "https://github.com/graalvm/graalvm-ce-builds/releases/download/graal-25.2.4/graalvm-community-jdk-25i2-25.0.4_linux-aarch64_bin.tar.gz".asUri

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

    private val String.systemProperty: String get() = System.getProperty(this)!!

    private val String.asUri: URI get() = URI.create(this)
}
