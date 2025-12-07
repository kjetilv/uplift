package org.apache.commons.logging;

import com.github.kjetilv.uplift.flogs.LogLevel;
import com.github.kjetilv.uplift.flogs.Logger;

import java.util.Objects;

class CommonsLogger implements Log {

    private final Logger logger;

    CommonsLogger(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void debug(Object message) {
        logger.log(LogLevel.DEBUG, String.valueOf(message));
    }

    @Override
    public void debug(Object message, Throwable t) {
        logger.log(LogLevel.DEBUG, String.valueOf(message), t);
    }

    @Override
    public void error(Object message) {
        logger.log(LogLevel.ERROR, String.valueOf(message));
    }

    @Override
    public void error(Object message, Throwable t) {
        logger.log(LogLevel.ERROR, String.valueOf(message), t);
    }

    @Override
    public void fatal(Object message) {
        logger.log(LogLevel.ERROR, String.valueOf(message));
    }

    @Override
    public void fatal(Object message, Throwable t) {
        logger.log(LogLevel.ERROR, String.valueOf(message), t);
    }

    @Override
    public void info(Object message) {
        logger.log(LogLevel.INFO, String.valueOf(message));
    }

    @Override
    public void info(Object message, Throwable t) {
        logger.log(LogLevel.INFO, String.valueOf(message), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(LogLevel.ERROR);
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isEnabled(LogLevel.ERROR);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(LogLevel.WARN);
    }

    @Override
    public void trace(Object message) {
        logger.log(LogLevel.TRACE, String.valueOf(message));
    }

    @Override
    public void trace(Object message, Throwable t) {
        logger.log(LogLevel.TRACE, String.valueOf(message), t);
    }

    @Override
    public void warn(Object message) {
        logger.log(LogLevel.WARN, String.valueOf(message));
    }

    @Override
    public void warn(Object message, Throwable t) {
        logger.log(LogLevel.WARN, String.valueOf(message), t);
    }
}
