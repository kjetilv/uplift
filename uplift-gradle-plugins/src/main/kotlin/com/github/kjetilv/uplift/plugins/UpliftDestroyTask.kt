package com.github.kjetilv.uplift.plugins

abstract class UpliftDestroyTask : UpliftTask() {

    override fun perform() {
        if (!cdkApp().isActualDirectory) {
            initCdkApp()
        }
        runCdk("cdk destroy --require-approval=never ${profileOption()} ${stack.get()}")
        clearCdkApp()
    }
}
