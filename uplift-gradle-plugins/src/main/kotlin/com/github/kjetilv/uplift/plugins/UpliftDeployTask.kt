package com.github.kjetilv.uplift.plugins

abstract class UpliftDeployTask : UpliftTask() {

    override fun perform() {
        collectLambdaZips()
        deploy()
    }
}
