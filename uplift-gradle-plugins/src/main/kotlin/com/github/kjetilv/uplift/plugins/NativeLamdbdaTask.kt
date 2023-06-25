package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.toPath

@CacheableTask
abstract class NativeLamdbdaTask : DefaultTask() {

    init {
        group = "uplift"
    }

    @get:Input
    abstract val identifier: Property<String>

    @get:Input
    abstract val jarTask: Property<String>

    @get:Input
    abstract val arch: Property<String>

    @get:Input
    abstract val jdkVersion: Property<String>

    @get:Input
    abstract val javaDist: Property<URI>

    @get:Input
    abstract val buildsite: Property<String>

    @get:OutputFile
    abstract val zipFile: Property<Path>

    @TaskAction
    fun perform() {
        val dist = javaDist.get()
        val distUri = dist.takeIf {
            it.scheme == "file"
        }
        val distFile = dist.takeIf {
            it.scheme =="https" || it.scheme == "http"
        }
        require(distFile != null || distUri != null) {
            "javaDist property must be file, http or https URI: $dist"
        }
        val split = renderResource(
            "Dockerfile-lambda.st4",
            "buildsite" to buildsite.get(),
            "target" to identifier.get(),
            "arch" to arch.get(),
            "distfile" to distUri?.toASCIIString(),
            "disturi" to distFile?.toASCIIString()
        )
        Files.write(uplift.resolve("Dockerfile"), split)
        logger.info("Created new DockerFile for ${buildsite.get()}")

        val shadow = outputs(requestedTask() + dependencyTasks()).firstOrNull { it.isJar }
            ?: throw IllegalStateException("No suitable jar found in dependencies")

        copyTo(shadow, uplift, target = "shadow.jar")
        if (dist.scheme == "file") {
            copyTo(dist.toPath(), uplift, target = "dist.tar.gz")
        }

        exe(uplift, "docker build --tag ${buildsite.get()}:latest $uplift")
        exe(uplift, "docker build --tag ${identifier.get()}:latest $uplift")
        exe(uplift, "docker run -v ${uplift.toAbsolutePath()}:/out ${buildsite.get()}:latest")

        zipFile(uplift.resolve(identifier.get()), zipFile = zipFile.get())
    }

    private fun requestedTask() = (jarTask.nonBlank?.let(::listOf) ?: emptyList())

    private val uplift: Path
        get() = project.buildDir.toPath().resolve("uplift").also(Files::createDirectories)
}
