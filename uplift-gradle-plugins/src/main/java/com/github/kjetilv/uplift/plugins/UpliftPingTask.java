package com.github.kjetilv.uplift.plugins;

import org.gradle.work.DisableCachingByDefault;

/** Reports the deployed stack. Read only. */
@DisableCachingByDefault(because = "Pinging is not cacheable")
public abstract class UpliftPingTask extends UpliftTask {

    @Override
    protected void perform() {
        report(null);
    }
}
