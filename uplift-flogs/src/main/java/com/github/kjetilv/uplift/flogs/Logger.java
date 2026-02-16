package com.github.kjetilv.uplift.flogs;

public interface Logger {

    default String name() {
        return name(false);
    }

    String name(boolean shorten);

    boolean isEnabled(LogLevel logLevel);

    void log(LogLevel logLevel, String format, Object... arguments);
}
