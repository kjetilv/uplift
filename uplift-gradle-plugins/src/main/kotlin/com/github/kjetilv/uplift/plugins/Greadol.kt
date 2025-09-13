package com.github.kjetilv.uplift.plugins

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

internal operator fun <T : Any> Property<T>.remAssign(value: T?): Unit = set(value)

internal operator fun <T : Any> ListProperty<T>.remAssign(values: Iterable<T>?): Unit = set(values)

internal fun Project.propertyOr(name: String, def: () -> String = { "" }): String =
    takeIf { hasProperty(name) }?.let { this.property(name) }?.toString() ?: def()

internal fun Project.buildSubDirectory(dir: String): Path =
    layout.buildDirectory.dir(dir).get().asFile.toPath()
        .also(Files::createDirectories)

internal inline fun <reified T : Task> Project.register(name: String, crossinline reg: T.() -> Unit) =
    this.also {
        tasks.register(name, T::class.java) { it.reg() }
    }

internal val Project.classpath: List<File> get() = configurations.first("$path:runtimeClasspath"::equals).toList()

internal fun Project.resolve(property: String) =
    System.getProperty(property)
        ?: System.getenv("UPLIFT_${property.uppercase()}")
        ?: propertyOr(property)

internal val Project.shortGroupName
    get() = (base().let { group ->
        group.lastIndexOf('.').takeIf { it > 0 }?.let { group.substring(it + 1) }
    } ?: base()).apply {
        if (isBlank()) {
            throw IllegalStateException("Blank shortname")
        }
    }

private fun Project.base() =
    this.group.toString()
        .takeIf { it.isNotBlank() } ?: Path.of(System.getProperty("user.dir"))
        .toAbsolutePath()
        .last()
        .toString()

internal val Property<String>.nonBlank: String? get() = orNull?.takeIf { it.isNotBlank() }

internal fun Task.dependencyOutputs() = outputs(dependencyTasks())

internal fun Task.outputs(dependencyTasks: List<Any>) = dependencyTasks.flatMap { dep ->
    files(dep)?.toList()?.mapNotNull(File::toPath)?.toList() ?: emptyList()
}.takeIf { it.isNotEmpty() } ?: emptyList()

internal fun Task.dependencyTasks() = dependsOn.toList().map(Any::toString)

private fun Task.files(dependOn: Any): MutableSet<File>? =
    this.project.tasks.findByPath(dependOn.toString())?.outputs?.files?.files
