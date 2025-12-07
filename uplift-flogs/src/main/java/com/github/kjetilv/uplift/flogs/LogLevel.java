package com.github.kjetilv.uplift.flogs;

public enum LogLevel {
    NONE,
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE;

    public static final LogLevel DEFAULT = INFO;
}
