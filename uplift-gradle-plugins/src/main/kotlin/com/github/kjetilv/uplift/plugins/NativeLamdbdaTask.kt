package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.toPath

abstract class NativeLamdbdaTask : DefaultTask() {
    init {
        group = "uplift"
    }

    @get:Input
    abstract val identifier: Property<String>

    @get:Input
    abstract val arch: Property<String>

    @get:Input
    abstract val jdkVersion: Property<String>

    @get:Input
    abstract val graalVersion: Property<String>

    @get:Input
    abstract val javaDist: Property<URI>

    @get:Input
    abstract val javaHome: Property<String>

    @get:Input
    abstract val buildsite: Property<String>

    @get:OutputFile
    abstract val zipFile: Property<Path>

    @TaskAction
    fun perform() {
        val split = renderResource(
            "Dockerfile-lambda.st4",
            "buildsite" to buildsite.get(),
            "target" to identifier.get(),
            "arch" to arch.get(),
            "disturi" to javaDist.get().takeIf { it.scheme != "file" }?.toASCIIString(),
            "distdir" to javaHome.get()
        )
        Files.write(uplift.resolve("Dockerfile"), split)
        logger.info("Created new DockerFile for ${buildsite.get()}")

        val shadow = dependencyOutputs()?.firstOrNull { it.isJar }
            ?: throw IllegalStateException("No shadowJar found in dependencies")

        copyTo(shadow, uplift, target = "shadow.jar")
        if (javaDist.get().scheme == "file") {
            copyTo(javaDist.get().toPath(), uplift, target = "dist.tar.gz")
        }

        exe(uplift, "docker build --tag ${buildsite.get()}:latest $uplift")
        exe(uplift, "docker build --tag ${identifier.get()}:latest $uplift")
        exe(uplift, "docker run -v ${uplift.toAbsolutePath()}:/out ${buildsite.get()}:latest")

        zipFile(uplift.resolve(identifier.get()), zipFile = zipFile.get())
    }

    private val uplift: Path
        get() = project.buildDir.toPath().resolve("uplift").also(Files::createDirectories)
}
