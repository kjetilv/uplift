package org.slf4j;

import org.slf4j.event.Level;

import static com.github.kjetilv.uplift.flogs.LogLevel.DEBUG;
import static com.github.kjetilv.uplift.flogs.LogLevel.ERROR;
import static com.github.kjetilv.uplift.flogs.LogLevel.INFO;
import static com.github.kjetilv.uplift.flogs.LogLevel.TRACE;
import static com.github.kjetilv.uplift.flogs.LogLevel.WARN;

class FLogger implements Logger {

    private final String name;

    private final com.github.kjetilv.uplift.flogs.Logger l;

    FLogger(String name, com.github.kjetilv.uplift.flogs.Logger l) {
        this.name = name;
        this.l = l;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabledForLevel(Level level) {
        return l.isEnabled(switch (level) {
            case ERROR -> ERROR;
            case WARN -> WARN;
            case INFO -> INFO;
            case DEBUG -> DEBUG;
            case TRACE -> TRACE;
        });
    }

    @Override
    public void trace(String format, Object... arguments) {
        l.log(TRACE, format, arguments);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return l.isEnabled(TRACE);
    }

    @Override
    public void debug(String format, Object... arguments) {
        l.log(DEBUG, format, arguments);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return l.isEnabled(DEBUG);
    }

    @Override
    public void info(String format, Object... arguments) {
        l.log(INFO, format, arguments);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return l.isEnabled(INFO);
    }

    @Override
    public void warn(String format, Object... arguments) {
        l.log(WARN, format, arguments);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return l.isEnabled(WARN);
    }

    @Override
    public void error(String format, Object... arguments) {
        l.log(ERROR, format, arguments);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return l.isEnabled(ERROR);
    }
}
