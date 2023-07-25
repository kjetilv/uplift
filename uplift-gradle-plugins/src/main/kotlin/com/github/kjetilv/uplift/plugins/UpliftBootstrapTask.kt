package com.github.kjetilv.uplift.plugins

import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import java.nio.file.Path

@CacheableTask
abstract class UpliftBootstrapTask : UpliftLambdaZipTask() {

    @get:OutputFile
    abstract val template: Property<Path>

    override fun perform() {
        collectLambdaZips()
        bootstrapCdk()
    }

    private fun bootstrapCdk() {
        runCdk("cdk bootstrap ${profileOption()} aws://${account.get()}/${region.get()}")
    }
}
