package org.apache.logging.log4j;

import module java.base;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;

/// A Log4j logger that logs through a flogs logger. A formatter logger formats messages with
/// [String#format(String, Object...)], and passes only a last [Throwable] parameter on to flogs.
final class Log4jLogger implements Logger {

    private final String name;

    private final com.github.kjetilv.uplift.flogs.Logger logger;

    private final boolean formatter;

    Log4jLogger(String name, com.github.kjetilv.uplift.flogs.Logger logger, boolean formatter) {
        this.name = Objects.requireNonNull(name, "name");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.formatter = formatter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Level getLevel() {
        return switch (Flogs.logLevel()) {
            case NONE -> Level.OFF;
            case ERROR -> Level.ERROR;
            case WARN -> Level.WARN;
            case INFO -> Level.INFO;
            case DEBUG -> Level.DEBUG;
            case TRACE -> Level.TRACE;
        };
    }

    @Override
    public boolean isEnabled(Level level, Marker marker) {
        var flogsLevel = flogsLevel(level);
        return flogsLevel != null && logger.isEnabled(flogsLevel);
    }

    @Override
    public void log(Level level, Marker marker, String message, Object... params) {
        var flogsLevel = flogsLevel(level);
        if (flogsLevel == null) {
            return;
        }
        if (formatter) {
            if (logger.isEnabled(flogsLevel)) {
                var thrown = Log4jSupport.lastThrowable(params);
                logger.log(
                    flogsLevel,
                    Log4jSupport.printf(message, params),
                    thrown == null ? Log4jSupport.NO_PARAMS : new Object[] {thrown}
                );
            }
        } else {
            logger.log(flogsLevel, message, params);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + (formatter ? " formatter" : "") + "]";
    }

    /// @return The nearest flogs level, or null for OFF and null
    private static LogLevel flogsLevel(Level level) {
        if (level == null || level.intLevel() <= Level.OFF.intLevel()) {
            return null;
        }
        var intLevel = level.intLevel();
        return intLevel <= Level.ERROR.intLevel() ? LogLevel.ERROR
            : intLevel <= Level.WARN.intLevel() ? LogLevel.WARN
                : intLevel <= Level.INFO.intLevel() ? LogLevel.INFO
                    : intLevel <= Level.DEBUG.intLevel() ? LogLevel.DEBUG
                        : LogLevel.TRACE;
    }
}
