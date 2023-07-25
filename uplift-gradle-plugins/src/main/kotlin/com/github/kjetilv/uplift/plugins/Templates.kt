package com.github.kjetilv.uplift.plugins

import org.stringtemplate.v4.ST
import java.nio.charset.StandardCharsets

internal fun renderResource(resource: String, vararg pars: Pair<String, String?>): List<String> =
    render(loadResource(resource), pars)

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

