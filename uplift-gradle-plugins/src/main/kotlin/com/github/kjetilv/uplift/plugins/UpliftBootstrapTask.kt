package com.github.kjetilv.uplift.plugins

import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import java.nio.file.Path

@CacheableTask
abstract class UpliftBootstrapTask : UpliftTask() {

    @get:OutputFile
    abstract val template: Property<Path>

    override fun perform() {
        collectLambdaZips()
        bootstrapCdk()
    }
}
