package com.github.kjetilv.uplift.plugins

import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.process.ExecOperations
import java.nio.file.Path
import javax.inject.Inject

@CacheableTask
abstract class UpliftInitTask @Inject constructor(execOperations: ExecOperations) : UpliftCdkTask(execOperations) {

    @get:OutputFile
    abstract val activeJar: Property<Path>

    @get:OutputFile
    abstract val activePom: Property<Path>

    override fun perform() = initCdkApp()
}
