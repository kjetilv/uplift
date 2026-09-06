package com.github.kjetilv.uplift.plugins.core;

/**
 * Logging seam, so that the core carries no build system types. Gradle tasks and Maven
 * mojos each supply their own implementation.
 */
public interface Log {

    void info(String message);

    void warn(String message);

    void lifecycle(String message);
}
