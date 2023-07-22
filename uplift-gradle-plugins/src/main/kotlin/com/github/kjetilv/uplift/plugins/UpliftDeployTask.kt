package com.github.kjetilv.uplift.plugins

abstract class UpliftDeployTask : UpliftLambdaZipTask() {

    override fun perform() {
        collectLambdaZips()
        deploy()
        ping()
    }
}
