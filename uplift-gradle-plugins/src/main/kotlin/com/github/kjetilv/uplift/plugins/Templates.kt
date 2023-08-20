package com.github.kjetilv.uplift.plugins

import org.stringtemplate.v4.ST
import java.io.InputStream
import java.nio.charset.StandardCharsets

internal fun String.renderResource(vararg pars: Pair<String, String?>) =
    render(loadResource(this), pars)

internal fun loadResource(template: String) =
    inputStream(template)?.use {
        String(it.readBytes(), StandardCharsets.UTF_8)
    } ?: throw IllegalStateException("No template `$template` in path")

private fun render(loadTemplate: String, pars: Array<out Pair<String, String?>>) =
    ST(loadTemplate, '⎨', '⎬')
        .apply {
            pars.forEach { (key, value) ->
                value?.also { this.add(key, value) }
            }
        }
        .render()
        .split("\n")
        .filter { it.isNotBlank() }

private fun inputStream(template: String): InputStream? =
    Thread.currentThread().contextClassLoader.getResourceAsStream(template)

