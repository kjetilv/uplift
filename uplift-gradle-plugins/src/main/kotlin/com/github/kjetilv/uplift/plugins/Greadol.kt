package com.github.kjetilv.uplift.plugins

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import java.io.File
import java.nio.file.Path

internal operator fun <T> Property<T>.remAssign(value: T): Unit = set(value)

internal val Project.shortGroupName
    get() =
        (base().let { group ->
            group.lastIndexOf('.').takeIf { it > 0 }
                ?.let { group.substring(it + 1) }
        } ?: base()).apply {
            if (isBlank()) {
                throw IllegalStateException("Blank shortname")
            }
        }

private fun Project.base() =
    this.group.toString().takeIf { it.isNotBlank() }
        ?: Path.of(System.getProperty("user.dir")).toAbsolutePath().last().toString()

internal val Property<String>.nonBlank: String? get() = orNull?.toString()?.takeIf { it.isNotBlank() }

internal fun Task.dependencyOutputs(): List<Path>? = dependsOn.toList().flatMap { dep ->
    files(dep)?.toList()?.mapNotNull(File::toPath)?.toList() ?: emptyList()
}.takeIf { it.isNotEmpty() }

private fun Task.files(dependOn: Any): MutableSet<File>? =
    this.project.tasks.findByPath(dependOn.toString())?.outputs?.files?.files
