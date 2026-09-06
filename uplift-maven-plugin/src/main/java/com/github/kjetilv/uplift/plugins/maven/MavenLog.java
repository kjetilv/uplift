package com.github.kjetilv.uplift.plugins.maven;

import com.github.kjetilv.uplift.plugins.core.Log;

/** Binds the core's logging seam to Maven. Maven has no lifecycle level, so info is used. */
final class MavenLog implements Log {

    private final org.apache.maven.plugin.logging.Log log;

    MavenLog(org.apache.maven.plugin.logging.Log log) {
        this.log = log;
    }

    @Override
    public void info(String message) {
        log.debug(message);
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void lifecycle(String message) {
        log.info(message);
    }
}
