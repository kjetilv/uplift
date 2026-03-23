package com.github.kjetilv.uplift.plugins

import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Pinging is not cacheable")
abstract class UpliftPingTask @Inject constructor(execOperations: ExecOperations) : UpliftTask(execOperations) {

    override fun perform() = ping()
}
