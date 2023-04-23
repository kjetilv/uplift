package com.github.kjetilv.uplift.plugins

import org.gradle.api.tasks.TaskAction

abstract class UpliftDestroyTask : AbstractUpliftTask() {

    @TaskAction
    fun perform() {
        initialize()
        runCdk(command = "cdk destroy ${stack.get()} --require-approval=never")
    }
}
