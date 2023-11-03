package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
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
    abstract val main: Property<String>

    @get:Input
    abstract val javaDist: Property<URI>

    @get:Input
    abstract val buildsite: Property<String>

    @get:OutputFile
    abstract val zipFile: Property<Path>

    @get:OutputFile
    abstract val bootstrapFile: Property<Path>

    @get:Classpath
    abstract val classPath: ListProperty<File>

    @TaskAction
    fun perform() {
        val dist = javaDist.get()

        val distUri = dist.takeIf {
            it.scheme == "file"
        }

        val distFile = dist.takeIf {
            it.scheme == "https" || it.scheme == "http"
        }

        require(distFile != null || distUri != null) {
            "javaDist property must be file, http or https URI: $dist"
        }

        val cp =
            if (classPath.isPresent) classPath.get().map(File::toPath)
            else outputs(dependencyTasks()).filter { it.isJar || it.isDir }

        if (cp.isEmpty()) {
            logger.warn("No output jars from any upstream dependencies: ${dependencyTasks().joinToString(", ")}")
        }

        cp.forEach { copyTo(it, upliftClasspath) }

        val split = "Dockerfile-lambda.st4".renderResource(
            "buildsite" to buildsite.get(),
            "target" to identifier.get(),
            "arch" to arch.get(),
            "main" to main.get(),
            "distfile" to distUri?.toASCIIString(),
            "disturi" to distFile?.toASCIIString(),
            "classpath" to cp.joinToString(":") {
                "/out/classpath/${it.fileName}"
            }
        )

        Files.write(uplift.resolve("Dockerfile"), split)
        logger.info("Created new DockerFile for ${buildsite.get()}")

        if (dist.scheme == "file") {
            copyTo(dist.toPath(), uplift, target = "dist.tar.gz")
        }

        exe(uplift, "docker build --tag ${buildsite.get()}:latest $uplift")
        exe(uplift, "docker build --tag ${identifier.get()}:latest $uplift")
        exe(uplift, "docker run -v ${uplift.toAbsolutePath()}:/out ${buildsite.get()}:latest")

        zipFile(uplift.resolve(identifier.get()), zipFile = zipFile.get())
    }

    private val uplift: Path get() = project.buildSubDirectory("uplift")

    private val upliftClasspath: Path get() = project.buildSubDirectory("uplift/classpath")
}
