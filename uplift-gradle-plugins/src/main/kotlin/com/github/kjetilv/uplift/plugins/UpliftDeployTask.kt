package com.github.kjetilv.uplift.plugins

import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class UpliftDeployTask @Inject constructor(execOperations: ExecOperations) :
    UpliftLambdaZipTask(execOperations) {

    override fun perform() {
        collectLambdaZips()
        deploy()
        ping(lambdas() ?: emptyList())
    }

    private fun deploy() {
        runCdk("cdk deploy ${profileOption()} --require-approval=never ${stack.get()}")
    }
}
