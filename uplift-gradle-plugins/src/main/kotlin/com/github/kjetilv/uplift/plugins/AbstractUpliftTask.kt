@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.kjetilv.uplift.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

abstract class AbstractUpliftTask : DefaultTask() {

    init {
        group = "uplift"
    }

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
    abstract val profile: Property<String>

    @get:Input
    abstract val region: Property<String>

    @get:Input
    abstract val stack: Property<String>

    @get:InputFile
    abstract val stackbuilderJar: Property<Path>

    @get:Input
    abstract val stackbuilderClass: Property<String>

    internal fun upliftDir(): Path =
        project.buildDir.toPath().resolve("uplift").also(Files::createDirectories)

    @Suppress("SameParameterValue")
    internal fun runDocker(cwd: Path, container: String, cmd: String) =
        exe(cwd,
            "docker run " +
                    "-v ${awsAuth.get()}:/root/.aws " +
                    "-v ${cdkApp()}:/opt/app " +
                    "-v ${upliftDir()}:/lambdas " +
                    (env.get()
                        ?: emptyMap()).entries.joinToString { (key, value) ->
                        "-e $key=$value "
                    } +
                    "$container $cmd")

    internal fun profileOption() =
        profile.orNull?.let { "--profile=$it" }

    internal fun cdkApp() =
        project.buildDir.toPath().resolve("cdk-app").also(Files::createDirectories)

    internal fun resolvedStackbuilderJar() =
        stackbuilderJar.orNull ?: throw IllegalStateException("Required a stackbuilderJar")

    internal fun bootstrapCdk() {
        clearRecursive(cdkApp())
        runDocker(
            upliftDir(),
            "cdk-site:latest",
            "cdk init --language=java --generate-only"
        )
        clearRecursive(listOf(cdkApp().resolve("src/main/java/com"), cdkApp().resolve("src/test/java/com")))
        val jar = resolvedStackbuilderJar()
        copyTo(jar, cdkApp())
        val writeCdkCode = loadResource("CloudApp.java").split('\n')
        val sourcePackage = cdkApp().resolve("src/main/java/lambda/uplift/app")
        Files.createDirectories(sourcePackage)
        Files.write(sourcePackage.resolve("CloudApp.java"), writeCdkCode)

        val pom = cdkApp().resolve("pom.xml")
        val pomCopy = Files.copy(pom, cdkApp().resolve("pom.xml.orig"))
        Files.write(pom, templated(pomCopy))
        clearRecursive(pomCopy)

        runDocker(
            upliftDir(),
            "cdk-site:latest",
            "cdk bootstrap ${profileOption()} aws://${account.get()}/${region.get()}"
        )
    }

    internal fun initialize() {
        Files.write(
            upliftDir().resolve("Dockerfile"), renderResource("Dockerfile-cdk.st4", "arch" to arch.get())
        )

        exe(cwd = upliftDir(), cmd = "docker build --tag cdk-site:latest ${upliftDir()}")
        bootstrapCdk()
    }

    private fun templated(pomCopy: Path?) =
        Files.lines(pomCopy, StandardCharsets.UTF_8).collect(Collectors.toList()).flatMap { line ->
            if (line.contains("<mainClass>"))
                addedMain(indent(line), line)
            else if (line.contains("</dependencies>"))
                addedDeps(indent(line), line)
            else
                listOf(line)
        }

    private fun property(indent: String, key: String, property: Property<*>) =
        property(indent, key, property.get())

    private fun property(indent: String, key: String, value: Any?) =
        "$indent  <systemProperty><key>$key</key><value>$value</value></systemProperty>"

    private fun indent(main: String) = main.takeWhile(Char::isWhitespace)

    private fun clearRecursive(vararg paths: Path): Unit = clearRecursive(paths.toList())

    private fun clearRecursive(paths: List<Path>): Unit =
        paths.forEach { path ->
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

    private fun addedMain(indent: String, line: String) = listOf(
        line.replace("com.myorg.AppApp", "lambda.uplift.app.CloudApp"),
        "$indent<systemProperties>",
        property("$indent  ", "uplift.account", account),
        property("$indent  ", "uplift.region", region),
        property("$indent  ", "uplift.stack", stack),
        property("$indent  ", "uplift.stackbuilderJar", resolvedStackbuilderJar().fileName),
        property("$indent  ", "uplift.stackbuilderClass", stackbuilderClass),
        "$indent</systemProperties>"
    )

    private fun addedDeps(indent: String, line: String) =
        dependencies().filterNotNull().filterNot(::isAws).flatMap { dep ->
            dep.run {
                listOf(
                    "",
                    "$indent    <dependency>",
                    "$indent      <groupId>$group</groupId>",
                    "$indent      <artifactId>$name</artifactId>",
                    "$indent      <version>$version</version>",
                    "$indent    </dependency>"
                )
            }
        } + listOf(line)

    private fun isAws(dep: Dependency) =
        setOf(
            "software.amazon.awscdk" to "aws-cdk-lib",
            "software.constructs" to "constructs"
        ).contains(dep.group to dep.name)

    private fun dependencies() =
        project.configurations.findByName("compileClasspath")?.incoming?.dependencies?.stream()?.toList()?.toList()
            ?: emptyList()
}
