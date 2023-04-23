package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

abstract class AbstractUpliftTask : DefaultTask() {

    @get:Input
    abstract val arch: Property<String>

    @get:Input
    abstract val env: MapProperty<String, String>

    @get:InputFiles
    abstract val lambdaZips: ListProperty<Path>

    @get:Input
    abstract val awsAuth: Property<String>

    @get:Input
    abstract val account: Property<String>

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val stack: Property<String>

    @get:InputFile
    abstract val stackbuilderJar: Property<Path>

    @get:Input
    abstract val stackbuilderClass: Property<String>

    @get:Internal
    protected val uplift: Path
        get() = project.buildDir.toPath().resolve("uplift").also(Files::createDirectories)

    protected fun runCdk(cwd: Path = uplift, command: String) =
        runDocker(cwd, "cdk-site:latest", "$command --ci=true")

    @Suppress("SameParameterValue")
    private fun runDocker(cwd: Path, container: String, finalCommand: String) {
        exe(cwd,
            "docker run " +
                    "-v ${awsAuth.get()}:/root/.aws " +
                    "-v $cdkApp:/opt/app " +
                    "-v $uplift:/lambdas " +
                    (env.get()
                        ?: emptyMap()).entries.joinToString { (key, value) ->
                        "-e $key=$value "
                    } +
                    "$container $finalCommand")
    }

    private val cdkApp: Path
        get() = project.buildDir.toPath().resolve("cdk-app").also(Files::createDirectories)

    private val resolvedStackbuilderJar: Path
        get() = stackbuilderJar.orNull ?: throw IllegalStateException("Required a stackbuilderJar")

    private fun bootstrapCdk() {
        clearRecursive(cdkApp)
        runCdk(command = "cdk init --language=java --generate-only")
        clearRecursive(listOf("src/main/java/com", "src/test/java/com").map<String, Path>(cdkApp::resolve))

        val jar = resolvedStackbuilderJar
        copyTo(jar, cdkApp)
        val writeCdkCode = loadResource("CloudApp.java").split('\n')
        val sourcePackage = cdkApp.resolve("src/main/java/lambda/uplift/app")
        Files.createDirectories(sourcePackage)
        Files.write(sourcePackage.resolve("CloudApp.java"), writeCdkCode)

        val pom = cdkApp.resolve("pom.xml")
        val pomCopy = cdkApp.resolve("pom.xml.orig")
        Files.copy(pom, pomCopy)
        Files.write(pom, replacedContent(pomCopy))
        clearRecursive(pomCopy)
        runCdk(command = "cdk bootstrap")
    }

    private fun replacedContent(pomCopy: Path?): List<String> =
        Files.lines(pomCopy, StandardCharsets.UTF_8).collect(Collectors.toList()).flatMap { line ->
            line.takeIf {
                it.contains("<mainClass>")
            }?.let { main ->
                val indent = indent(main)
                listOf(
                    main.replace("com.myorg.AppApp", "lambda.uplift.app.CloudApp"),
                    "$indent<systemProperties>",
                    property("$indent  ", "account", account),
                    property("$indent  ", "region", region),
                    property("$indent  ", "stack", stack),
                    property("$indent  ", "stackbuilderJar", resolvedStackbuilderJar.fileName),
                    property("$indent  ", "stackbuilderClass", stackbuilderClass),
                    "$indent</systemProperties>"
                )
            } ?: listOf(line)
        }

    private fun property(indent: String, key: String, property: Property<*>) =
        property(indent, key, property.get())

    private fun property(indent: String, key: String, value: Any?) =
        "$indent  <systemProperty><key>uplift.$key</key><value>$value</value></systemProperty>"

    private fun indent(main: String) = main.takeWhile(Char::isWhitespace)

    private fun clearRecursive(vararg paths: Path): Unit = clearRecursive(paths.toList())

    private fun clearRecursive(paths: List<Path>): Unit = paths.forEach { path ->
        path.takeIf(Files::isDirectory)?.also { dir ->
            dir.also {
                Files.list(it).forEach { file ->
                    clearRecursive(file)
                }
            }.let {
                dir.also(Files::delete)
            }
        } ?: path.also(Files::delete)
    }

    protected fun initialize() {
        writeDockerFile(arch.get(), uplift)

        exe(cwd = uplift, command = "docker build --tag cdk-site:latest $uplift")

        if (cdkApp.resolve("cdk.out").isActualDirectory) {
            logger.info("CDK already set up in $cdkApp, reusing it. To reset, remove directory or do a clean build")
        } else {
            logger.info("No CDK set up in $cdkApp, bootstrapping a new one...")
            bootstrapCdk()
        }
    }

    private fun writeDockerFile(arch: String, path: Path) {
        Files.write(
            path.resolve("Dockerfile"), renderResource(
                "Dockerfile-cdk.st4", "arch" to arch
            )
        )
    }
}
