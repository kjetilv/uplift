package com.github.kjetilv.uplift.plugins

abstract class UpliftDeployTask : UpliftLambdaZipTask() {

    override fun perform() {
        collectLambdaZips()
        deploy()
        ping(lambdas() ?: emptyList())
    }

    private fun deploy() {
        runCdk("cdk deploy ${profileOption()} --require-approval=never ${stack.get()}")
    }
}
