package com.github.kjetilv.uplift.plugins.maven;

import org.apache.maven.plugins.annotations.Mojo;

/** Reports the deployed stack. Read only. */
@Mojo(name = "ping", requiresProject = true, threadSafe = true)
public class PingMojo extends AbstractUpliftMojo {

    @Override
    protected void perform() {
        report(null);
    }
}
