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
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

abstract class UpliftTask : DefaultTask() {

    init {
        group = "uplift"
    }

    @get:Input
    abstract val arch: Property<String>

    @get:Input
    abstract val env: MapProperty<String, String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
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
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val stackbuilderJar: Property<Path>

    @get:Input
    abstract val stackbuilderClass: Property<String>

    @TaskAction
    fun upliftPerform() {
        selfCheck()
        initialize()
        perform()
    }

    @Suppress("unused")
    fun env(vararg envs: Pair<String, String>): UpliftTask = this.apply {
        env.set(mapOf(*envs))
    }

    protected fun upliftDir(): Path =
        project.buildDir.toPath().resolve("uplift").also(Files::createDirectories)

    protected fun clearCdkApp() = clearRecursive(cdkApp())

    protected fun runCdk(cmd: String): ExecResult = exe(
        upliftDir(),
        "docker run " +
                volumes(
                    awsAuth.get() to "/root/.aws",
                    cdkApp() to "/opt/app",
                    upliftDir() to "/lambdas"
                ) +
                environment(
                    env.get()
                ) +
                "${"cdk-site:latest"} $cmd"
    )

    @Suppress("unused")
    fun configure(
        account: String? = null,
        region: String? = null,
        stack: String? = null,
        profile: String? = null
    ): UpliftTask = this.apply {
        account?.also(this.account::set)
        region?.also(this.region::set)
        stack?.also(this.stack::set)
        profile?.also(this.profile::set)
    }

    @Suppress("unused")
    fun stackWith(name: String) =
        this.apply {
            stackbuilderClass %= name
        }

    protected abstract fun perform()

    protected fun bootstrapCdk() {
        runCdk("cdk bootstrap ${profileOption()} aws://${account.get()}/${region.get()}")
    }

    protected fun profileOption() =
        profile.orNull?.let { "--profile=$it" }

    protected fun cdkApp(): Path = project.cdkApp()

    private fun resolvedStackbuilderJar() =
        stackbuilderJar.orNull ?: throw IllegalStateException("Required a stackbuilderJar")

    protected fun collectLambdaZips() =
        lambdas()?.forEach { lambdaZip ->
            copyTo(lambdaZip, upliftDir())
        } ?: throw IllegalStateException(
            "No zips configured, and no zips found in ${dependsOn.joinToString(", ", transform = Any::toString)}"
        )

    protected fun deploy() {
        runCdk("cdk deploy ${profileOption()} --require-approval=never ${stack.get()}")
    }

    private fun selfCheck() {
        listOf(
            account, region, profile, stack
        ).filterNot { it.nonBlank != null }.takeIf { it.isNotEmpty() }?.also {
            throw IllegalStateException(
                "Missing config! Either use `configure` method, or provide gradle.properties:\n ${it.joinToString("\n ")}"
            )
        }
    }

    protected fun initCdkApp() {
        clearCdkApp()
        runCdk("cdk init --language=java --generate-only")
        copyTo(resolvedStackbuilderJar(), cdkApp())

        val writeCdkCode = loadResource("CloudApp.java").split('\n')
        val sourcePackage = cdkApp().resolve("src/main/java/lambda/uplift/app")
        Files.createDirectories(sourcePackage)
        Files.write(sourcePackage.resolve("CloudApp.java"), writeCdkCode)
        clearRecursive(listOf(cdkApp().resolve("src/main/java/com"), cdkApp().resolve("src/test/java/com")))

        val pom = cdkApp().resolve("pom.xml")
        val pomCopy = Files.copy(pom, cdkApp().resolve("pom.xml.orig"))
        Files.write(pom, templated(pomCopy))
        clearRecursive(pomCopy)
    }

    private fun initialize() {
        Files.write(
            upliftDir().resolve("Dockerfile"), renderResource("Dockerfile-cdk.st4", "arch" to arch.get())
        )
        exe(cwd = upliftDir(), cmd = "docker build --tag cdk-site:latest ${upliftDir()}")
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

    private fun lambdas(): List<Path>? =
        lambdaZips.get().takeIf { it.isNotEmpty() }?.toList()
            ?: dependencyOutputs().filter(Path::isZip).takeIf { it.isNotEmpty() }

    private fun volumes(vararg vols: Pair<*, *>) =
        vols.joinToString("") { (local, contained) ->
            "-v $local:$contained "
        }

    private fun environment(env: Map<String, String>?) =
        env?.entries?.joinToString("") { (key, value) ->
            "-e $key=$value "
        } ?: ""

    private fun property(indent: String, key: String, property: Property<*>) =
        property(indent, key, property.get())

    private fun property(indent: String, key: String, value: Any?) =
        "$indent  <systemProperty><key>$key</key><value>$value</value></systemProperty>"

    private fun indent(main: String) = main.takeWhile(Char::isWhitespace)

    protected fun clearRecursive(vararg paths: Path): Unit = clearRecursive(paths.toList())

    protected fun clearRecursive(paths: List<Path>): Unit =
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
