package com.github.kjetilv.uplift.plugins

import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

abstract class UpliftCdkTask : UpliftTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val stackbuilderJar: Property<Path>

    @get:Input
    abstract val stackbuilderClass: Property<String>

    override fun stackWith(name: String) {
        this.apply {
            stackbuilderClass %= name
        }
    }

    protected fun initCdkApp() {
        clearCdkApp()
        runCdk("cdk init --language=java --generate-only")
        copyTo(resolvedStackBuilderJar(), cdkApp())

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


    private fun templated(pomCopy: Path?) =
        Files.lines(pomCopy, StandardCharsets.UTF_8).collect(Collectors.toList()).flatMap { line ->
            if (line.contains("<mainClass>"))
                addedMain(indent(line), line)
            else if (line.contains("</dependencies>"))
                addedDeps(indent(line), line)
            else
                listOf(line)
        }

    private fun indent(main: String) = main.takeWhile(Char::isWhitespace)

    private fun addedMain(indent: String, line: String) = listOf(
        line.replace("com.myorg.AppApp", "lambda.uplift.app.CloudApp"),
        "$indent<systemProperties>",
        property("$indent  ", "uplift.account" to account.get()),
        property("$indent  ", "uplift.region" to region.get()),
        property("$indent  ", "uplift.stack" to stack.get()),
        property("$indent  ", "uplift.stackbuilderJar" to resolvedStackBuilderJar().fileName),
        property("$indent  ", "uplift.stackbuilderClass" to stackbuilderClass.get()),
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

    private fun dependencies() =
        project.configurations.findByName("compileClasspath")?.incoming?.dependencies?.stream()?.toList()?.toList()
            ?: emptyList()

    private fun property(indent: String, entry: Pair<String, Any>) =
        entry.let { (key, value) ->
            "$indent  <systemProperty><key>${key}</key><value>${value}</value></systemProperty>"
        }

    private fun resolvedStackBuilderJar() =
        stackbuilderJar.orNull ?: throw IllegalStateException("Required a stackbuilderJar")

    private fun isAws(dep: Dependency) =
        setOf(
            "software.amazon.awscdk" to "aws-cdk-lib",
            "software.constructs" to "constructs"
        ).contains(dep.group to dep.name)
}
