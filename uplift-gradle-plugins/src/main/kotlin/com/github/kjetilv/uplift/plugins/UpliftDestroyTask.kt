package com.github.kjetilv.uplift.plugins

import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Destroying a stack is not cacheable")
abstract class UpliftDestroyTask @Inject constructor(execOperations: ExecOperations) : UpliftCdkTask(execOperations) {

    override fun perform() {
        if (!cdkApp().isActualDirectory) {
            initCdkApp()
        }
        runCdk("cdk destroy --require-approval=never ${profileOption()} ${stack.get()}")
        clearCdkApp()
    }
}
