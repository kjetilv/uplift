package com.github.kjetilv.uplift.plugins

import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class UpliftPingTask @Inject constructor(execOperations: ExecOperations) : UpliftTask(execOperations) {

    override fun perform() = ping()
}
