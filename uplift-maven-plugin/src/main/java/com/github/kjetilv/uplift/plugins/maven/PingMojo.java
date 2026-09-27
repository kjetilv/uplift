package com.github.kjetilv.uplift.plugins.maven;

import org.apache.maven.plugins.annotations.Mojo;

/// Reports the deployed stack. Read only.
@SuppressWarnings("unused")
@Mojo(name = "ping", threadSafe = true)
public class PingMojo extends AbstractUpliftMojo {

    @Override
    protected void perform() {
        report(null);
    }
}
