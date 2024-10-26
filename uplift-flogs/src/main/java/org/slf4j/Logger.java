package org.slf4j;

import org.slf4j.event.Level;

@SuppressWarnings({"RedundantArrayCreation", "unused"})
public interface Logger {

    default boolean isTraceEnabled() {
        return isEnabledForLevel(Level.TRACE);
    }

    default void trace(String msg) {
        trace((Marker) null, msg);
    }

    default void trace(String format, Object arg) {
        trace(format, new Object[] {arg});
    }

    default void trace(String format, Object arg1, Object arg2) {
        trace(format, new Object[] {arg1, arg2});
    }

    default void trace(String msg, Throwable t) {
        trace(msg, new Object[] {t});
    }

    default void trace(Marker marker, String msg) {
        trace(marker, msg, NONE);
    }

    default void trace(Marker marker, String format, Object arg) {
        trace(marker, format, new Object[] {arg});
    }

    default void trace(Marker marker, String format, Object arg1, Object arg2) {
        trace(marker, format, new Object[] {arg1, arg2});
    }

    default void trace(Marker marker, String msg, Throwable t) {
        trace(marker, msg, new Object[] {t});
    }

    default void trace(Marker marker, String format, Object... argArray) {
        trace(format, argArray);
    }

    default boolean isDebugEnabled() {
        return isEnabledForLevel(Level.DEBUG);
    }

    default void debug(String msg) {
        debug((Marker) null, msg);
    }

    default void debug(String format, Object arg) {
        debug(format, new Object[] {arg});
    }

    default void debug(String format, Object arg1, Object arg2) {
        debug(format, new Object[] {arg1, arg2});
    }

    default void debug(String msg, Throwable t) {
        debug(msg, new Object[] {t});
    }

    default void debug(Marker marker, String msg) {
        debug(marker, msg, NONE);
    }

    default void debug(Marker marker, String format, Object arg) {
        debug(marker, format, new Object[] {arg});
    }

    default void debug(Marker marker, String format, Object arg1, Object arg2) {
        debug(marker, format, new Object[] {arg1, arg2});
    }

    default void debug(Marker marker, String msg, Throwable t) {
        debug(marker, msg, new Object[] {t});
    }

    default void debug(Marker marker, String format, Object... argArray) {
        debug(format, argArray);
    }

    default boolean isInfoEnabled() {
        return isEnabledForLevel(Level.INFO);
    }

    default void info(String msg) {
        info((Marker) null, msg);
    }

    default void info(String format, Object arg) {
        info(format, new Object[] {arg});
    }

    default void info(String format, Object arg1, Object arg2) {
        info(format, new Object[] {arg1, arg2});
    }

    default void info(String msg, Throwable t) {
        info(msg, new Object[] {t});
    }

    default boolean isInfoEnabled(Marker marker) {
        return isEnabledForLevel(Level.INFO);
    }

    default void info(Marker marker, String msg) {
        info(marker, msg, NONE);
    }

    default void info(Marker marker, String format, Object arg) {
        info(marker, format, new Object[] {arg});
    }

    default void info(Marker marker, String format, Object arg1, Object arg2) {
        info(marker, format, new Object[] {arg1, arg2});
    }

    default void info(Marker marker, String msg, Throwable t) {
        info(marker, msg, new Object[] {t});
    }

    default void info(Marker marker, String format, Object... argArray) {
        info(format, argArray);
    }

    default boolean isWarnEnabled() {
        return isEnabledForLevel(Level.WARN);
    }

    default void warn(String msg) {
        warn((Marker) null, msg);
    }

    default void warn(String format, Object arg) {
        warn(format, new Object[] {arg});
    }

    default void warn(String format, Object arg1, Object arg2) {
        warn(format, new Object[] {arg1, arg2});
    }

    default void warn(String format, Object... arguments) {
    }

    default void warn(String msg, Throwable t) {
        warn(msg, new Object[] {t});
    }

    default boolean isWarnEnabled(Marker marker) {
        return isEnabledForLevel(Level.WARN);
    }

    default void warn(Marker marker, String msg) {
        warn(marker, msg, NONE);
    }

    default void warn(Marker marker, String format, Object arg) {
        warn(marker, format, new Object[] {arg});
    }

    default void warn(Marker marker, String format, Object arg1, Object arg2) {
        warn(marker, format, new Object[] {arg1, arg2});
    }

    default void warn(Marker marker, String msg, Throwable t) {
        warn(marker, msg, new Object[] {t});
    }

    default void warn(Marker marker, String format, Object... argArray) {
        warn(format, argArray);
    }

    default boolean isErrorEnabled() {
        return isEnabledForLevel(Level.ERROR);
    }

    default void error(String msg) {
        error((Marker) null, msg);
    }

    default void error(String format, Object arg) {
        error(format, new Object[] {arg});
    }

    default void error(String format, Object arg1, Object arg2) {
        error(format, new Object[] {arg1, arg2});
    }

    default void error(String format, Object... arguments) {
    }

    default void error(String msg, Throwable t) {
        error(msg, new Object[] {t});
    }

    default boolean isErrorEnabled(Marker marker) {
        return isEnabledForLevel(Level.ERROR);
    }

    default void error(Marker marker, String msg) {
        error(marker, msg, NONE);
    }

    default void error(Marker marker, String format, Object arg) {
        error(marker, format, new Object[] {arg});
    }

    default void error(Marker marker, String format, Object arg1, Object arg2) {
        error(marker, format, new Object[] {arg1, arg2});
    }

    default void error(Marker marker, String msg, Throwable t) {
        error(marker, msg, new Object[] {t});
    }

    default void error(Marker marker, String format, Object... argArray) {
        error(format, argArray);
    }

    default String getName() {
        return getClass().getName();
    }

    default boolean isEnabledForLevel(Level level) {
        return false;
    }

    default void trace(String format, Object... arguments) {
    }

    default boolean isTraceEnabled(Marker marker) {
        return isEnabledForLevel(Level.TRACE);
    }

    default void debug(String format, Object... arguments) {
    }

    default boolean isDebugEnabled(Marker marker) {
        return isEnabledForLevel(Level.DEBUG);
    }

    default void info(String format, Object... arguments) {
    }

    Object[] NONE = {};
}

