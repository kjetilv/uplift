package com.github.kjetilv.uplift.plugins

import org.gradle.api.tasks.TaskAction

abstract class UpliftDestroyTask : UpliftTask() {

    @TaskAction
    fun perform() {
        initialize()
        runDocker(
            upliftDir(),
            "cdk-site:latest",
            "cdk destroy --require-approval=never ${profileOption()} ${stack.get()}"
        )
    }
}
