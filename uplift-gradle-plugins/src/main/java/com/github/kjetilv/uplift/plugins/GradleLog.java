package com.github.kjetilv.uplift.plugins;

import com.github.kjetilv.uplift.plugins.core.Log;
import org.gradle.api.logging.Logger;

/** Binds the core's logging seam to Gradle. */
final class GradleLog implements Log {

    private final Logger logger;

    GradleLog(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void lifecycle(String message) {
        logger.lifecycle(message);
    }
}
