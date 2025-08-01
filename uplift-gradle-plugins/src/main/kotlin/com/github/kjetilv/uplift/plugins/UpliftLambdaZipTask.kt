package com.github.kjetilv.uplift.plugins

import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.ExecOperations
import java.nio.file.Path
import javax.inject.Inject

abstract class UpliftLambdaZipTask @Inject constructor(execOperations: ExecOperations) : UpliftTask(execOperations) {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val lambdaZips: ListProperty<Path>

    protected fun collectLambdaZips() =
        lambdas()?.forEach { lambdaZip ->
            copyTo(lambdaZip, upliftDir())
        } ?: throw IllegalStateException(
            "No zips configured, and no zips found in ${dependsOn.joinToString(", ", transform = Any::toString)}"
        )

    protected fun lambdas(): List<Path>? =
        lambdaZips.get().takeIf(nonEmpty())?.toList()
            ?: dependencyOutputs()
                .filter(Path::isZip)
                .takeIf(nonEmpty())
}
