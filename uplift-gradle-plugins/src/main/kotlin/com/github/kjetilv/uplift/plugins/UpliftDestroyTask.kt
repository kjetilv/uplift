package com.github.kjetilv.uplift.plugins

import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class UpliftDestroyTask @Inject constructor(execOperations: ExecOperations) : UpliftCdkTask(execOperations) {

    override fun perform() {
        if (!cdkApp().isActualDirectory) {
            initCdkApp()
        }
        runCdk("cdk destroy --require-approval=never ${profileOption()} ${stack.get()}")
        clearCdkApp()
    }
}
