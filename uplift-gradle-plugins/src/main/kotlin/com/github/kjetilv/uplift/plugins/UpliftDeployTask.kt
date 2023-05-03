package com.github.kjetilv.uplift.plugins

import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

abstract class UpliftDeployTask : UpliftTask() {

    @TaskAction
    fun perform() {
        selfCheck()
        collectLambdaZips()
        initialize()
        deploy()
    }

    private fun deploy() =
        runDocker(
            upliftDir(),
            "cdk-site:latest",
            "cdk deploy ${profileOption()} --require-approval=never ${stack.get()}"
        )

    private fun collectLambdaZips() =
        lambdas()?.forEach { lambdaZip ->
            copyTo(lambdaZip, upliftDir())
        } ?: throw IllegalStateException(
            "No zips configured, and no zips found in ${dependsOn.joinToString(", ", transform = Any::toString)}"
        )

    private fun lambdas(): List<Path>? =
        lambdaZips.get().takeIf { it.isNotEmpty() }?.toList()
            ?: dependencyOutputs().filter(Path::isZip).takeIf { it.isNotEmpty() }
}
