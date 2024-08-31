package com.github.kjetilv.uplift.plugins

import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import java.nio.file.Path

@CacheableTask
abstract class UpliftInitTask : UpliftCdkTask() {

    @get:OutputFile
    abstract val activeJar: Property<Path>

    @get:OutputFile
    abstract val activePom: Property<Path>

    override fun perform() = initCdkApp()
}
