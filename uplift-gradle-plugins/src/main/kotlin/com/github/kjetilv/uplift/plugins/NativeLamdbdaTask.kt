package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.toPath

@CacheableTask
abstract class NativeLamdbdaTask @Inject constructor(private var execOperations: ExecOperations) : DefaultTask() {

    init {
        group = "uplift"
    }

    @get:Input
    abstract val identifier: Property<String>

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

    @get:Input
    abstract val enablePreview: Property<Boolean>

    @get:Input
    abstract val addModules: Property<String>

    @get:Input
    abstract val otherOptions: Property<String>

    @TaskAction
    fun perform() {
        val dist = javaDist.get()

        val file = dist.takeIf {
            it.scheme == "file"
        }

        val uri = dist.takeIf {
            it.scheme == "https" || it.scheme == "http"
        }

        require(file != null || uri != null) {
            "javaDist property must be file, http or https URI: $dist"
        }

        val cp = runtimeClasspath + dependencyOutputs

        if (cp.isEmpty()) {
            logger.warn("No output jars from any upstream dependencies: ${dependencyTasks().joinToString(", ")}")
        }

        cp.forEach { copyTo(it, upliftClasspath) }

        val split = "lambda-st4/Dockerfile".renderResource(
            "buildsite" to buildsite.get(),
            "target" to identifier.get(),
            "arch" to arch.get(),
            "main" to main.get(),
            "enablepreview" to (if (enablePreview.isPresent && enablePreview.get()) "--enable-preview" else ""),
            "addmodules" to (if (addModules.isPresent) "--add-modules ${addModules.get()}" else ""),
            "otheroptions" to (if (otherOptions.isPresent) otherOptions.get() else ""),
            "distfile" to file?.toASCIIString(),
            "disturi" to uri?.toASCIIString(),
            "classpath" to cp.joinToString(":") {
                "/out/classpath/${it.fileName}"
            }
        )

        Files.write(uplift.resolve("Dockerfile"), split)
        logger.info("Created new DockerFile for ${buildsite.get()}")

        if (dist.scheme == "file") {
            copyTo(dist.toPath(), uplift, target = "dist.tar.gz")
        }

        docker(
            cwd = uplift,
            dockerCmd = "build --tag ${buildsite.get()}:latest $uplift",
            execOperations
        )
        docker(
            cwd = uplift,
            dockerCmd = "build --tag ${identifier.get()}:latest $uplift",
            execOperations
        )
        docker(
            cwd = uplift,
            dockerCmd = "run -v ${uplift.toAbsolutePath()}:/out ${buildsite.get()}:latest",
            execOperations
        )

        zipFile(uplift.resolve(identifier.get()), zipFile = zipFile.get())
    }

    private val runtimeClasspath get() = if (classPath.isPresent) classPath.get().map(File::toPath) else emptyList()

    private val dependencyOutputs get() = outputs(dependencyTasks()).filter { it.isJar || it.isDir }

    private val uplift: Path get() = project.buildSubDirectory("uplift")

    private val upliftClasspath: Path get() = project.buildSubDirectory("uplift/classpath")
}
