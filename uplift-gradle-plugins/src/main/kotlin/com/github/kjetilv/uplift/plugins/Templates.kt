package com.github.kjetilv.uplift.plugins

import org.stringtemplate.v4.ST
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

internal fun renderResource(resource: String, vararg pars: Pair<String, String?>): List<String> =
    render(loadResource(resource), pars)

internal fun renderFile(file: Path, vararg pars: Pair<String, String?>): List<String> =
    render(load(file), pars)

internal fun loadResource(template: String): String =
    Thread.currentThread().contextClassLoader.getResourceAsStream(template)
        ?.use {
            String(it.readBytes(), StandardCharsets.UTF_8)
        }
        ?: throw IllegalStateException("No template `$template` in path")

private fun render(
    loadTemplate: String,
    pars: Array<out Pair<String, String?>>
): List<String> {
    return ST(loadTemplate, '⎨', '⎬').apply {
        pars.forEach { (key, value) ->
            value?.also { this.add(key, value) }
        }
    }.render()
        .split("\n")
        .filter { it.isNotBlank() }
}

private fun load(template: Path): String =
    try {
        Files.newInputStream(template)
            .use {
                String(it.readBytes(), StandardCharsets.UTF_8)
            }
    } catch (e: Exception) {
        throw IllegalStateException("No path $template found")
    }
