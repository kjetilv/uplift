package com.github.kjetilv.uplift.plugins

import org.gradle.api.tasks.TaskAction
import java.nio.file.Path

abstract class UpliftTask : AbstractUpliftTask() {

    @TaskAction
    fun perform() {
        initialize()
        collectLambdaZips()
        deploy()
    }

    private fun deploy() =
        runCdk(command = "cdk deploy ${stack.get()} --require-approval=never")

    private fun collectLambdaZips() =
        lambdas()?.forEach { lambdaZip ->
            copyTo(lambdaZip, uplift)
        } ?: throw IllegalStateException(
            "No zips configured, and no zips found in ${dependsOn.joinToString(", ", transform = Any::toString)}"
        )

    @Suppress("unused")
    fun configure(
        account: String,
        region: String,
        stack: String,
    ) =
        this.apply {
            this.account %= account
            this.region %= region
            this.stack %= stack
        }

    @Suppress("unused")
    fun env(vararg envs: Pair<String, String>) =
        this.apply {
            env.set(mapOf(*envs))
        }

    @Suppress("unused")
    fun stackWith(name: String) =
        this.apply {
            stackbuilderClass.set(name)
        }

    private fun lambdas(): List<Path>? =
        lambdaZips.get().takeIf { it.isNotEmpty() }?.toList() ?: dependencyOutputs()?.filter(Path::isZip)
}
