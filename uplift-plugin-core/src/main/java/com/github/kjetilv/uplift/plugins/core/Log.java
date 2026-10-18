package com.github.kjetilv.uplift.plugins.core;

/**
 * Logging interface to isalate core from particular build tools.
 */
public interface Log {

    void info(String message);

    void warn(String message);

    void lifecycle(String message);
}
