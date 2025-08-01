package com.github.kjetilv.uplift.plugins

import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.process.ExecOperations
import java.nio.file.Path
import javax.inject.Inject

@CacheableTask
abstract class UpliftBootstrapTask @Inject constructor(execOperations: ExecOperations) :
    UpliftLambdaZipTask(execOperations) {

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
