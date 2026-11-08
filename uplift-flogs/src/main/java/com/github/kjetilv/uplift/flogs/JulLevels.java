package com.github.kjetilv.uplift.flogs;

import java.util.logging.Level;

/// Maps between [java.util.logging] levels and [LogLevel]
final class JulLevels {

    /// @return The nearest level: `SEVERE` is [LogLevel#ERROR], `WARNING` is [LogLevel#WARN], `INFO` and
    ///     `CONFIG` are [LogLevel#INFO], `FINE` is [LogLevel#DEBUG], and finer levels are [LogLevel#TRACE]
    static LogLevel toFlogs(Level level) {
        var value = level.intValue();
        if (value >= Level.SEVERE.intValue()) {
            return LogLevel.ERROR;
        }
        if (value >= Level.WARNING.intValue()) {
            return LogLevel.WARN;
        }
        if (value >= Level.CONFIG.intValue()) {
            return LogLevel.INFO;
        }
        if (value >= Level.FINE.intValue()) {
            return LogLevel.DEBUG;
        }
        return LogLevel.TRACE;
    }

    /// @return The JUL level that lets through exactly the records [#toFlogs(Level)] maps to the level or above
    static Level toJul(LogLevel level) {
        return switch (level) {
            case NONE -> Level.OFF;
            case ERROR -> Level.SEVERE;
            case WARN -> Level.WARNING;
            case INFO -> Level.CONFIG;
            case DEBUG -> Level.FINE;
            case TRACE -> Level.ALL;
        };
    }

    private JulLevels() {
    }
}
