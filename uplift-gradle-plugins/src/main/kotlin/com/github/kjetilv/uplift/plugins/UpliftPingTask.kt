package com.github.kjetilv.uplift.plugins

abstract class UpliftPingTask : UpliftTask() {

    override fun perform() = ping()
}
