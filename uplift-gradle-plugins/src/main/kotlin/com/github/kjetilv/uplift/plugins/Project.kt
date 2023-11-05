package com.github.kjetilv.uplift.plugins

import org.gradle.api.Project
import java.nio.file.Files
import java.nio.file.Path

internal fun Project.cdkApp(): Path =
    layout.buildDirectory.dir("cdk-app").get().asFile.toPath().also(Files::createDirectories)

internal fun Project.templateFile(task: UpliftTask): Path =
    cdkApp().resolve("cdk.out").resolve("${task.stack.get()}.template.json")

internal fun Project.cdkFile(path: Path): Path =
    cdkFile(path.fileName.toString())

internal fun Project.cdkFile(path: String): Path =
    cdkApp().resolve(path)
