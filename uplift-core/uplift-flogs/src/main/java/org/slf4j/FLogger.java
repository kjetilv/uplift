package org.slf4j;

import org.slf4j.event.Level;

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
        return switch (level) {
            case ERROR -> l.isErrorEnabled();
            case WARN -> l.isWarnEnabled();
            case INFO -> l.isInfoEnabled();
            case DEBUG -> l.isDebugEnabled();
            case TRACE -> l.isTraceEnabled();
        };
    }

    @Override
    public void trace(String format, Object... arguments) {
        l.trace(format, arguments);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return l.isTraceEnabled();
    }

    @Override
    public void debug(String format, Object... arguments) {
        l.debug(format, arguments);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return l.isDebugEnabled();
    }

    @Override
    public void info(String format, Object... arguments) {
        l.info(format, arguments);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return l.isInfoEnabled();
    }

    @Override
    public void warn(String format, Object... arguments) {
        l.warn(format, arguments);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return l.isWarnEnabled();
    }

    @Override
    public void error(String format, Object... arguments) {
        l.error(format, arguments);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return l.isErrorEnabled();
    }
}
