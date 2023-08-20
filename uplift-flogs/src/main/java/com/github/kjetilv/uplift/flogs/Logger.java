package com.github.kjetilv.uplift.flogs;

public interface Logger {

    String name();

    boolean isEnabled(LogLevel logLevel);

    void log(LogLevel logLevel, String format, Object... arguments);
}
