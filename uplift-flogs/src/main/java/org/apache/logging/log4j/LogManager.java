package org.apache.logging.log4j;

import module java.base;
import com.github.kjetilv.uplift.flogs.Flogs;

/// Gives Log4j loggers that log through flogs. There is one logger for each name.
@SuppressWarnings({"unused", "WeakerAccess"})
public final class LogManager {

    public static final String ROOT_LOGGER_NAME = "";

    /// @return The logger for the calling class
    public static Logger getLogger() {
        return getLogger(caller());
    }

    /// @param clazz Class. Null gives the logger for the calling class.
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz == null ? caller() : clazz.getName());
    }

    /// @param value Object whose class names the logger. Null gives the logger for the calling class.
    public static Logger getLogger(Object value) {
        return getLogger(value == null ? caller() : value.getClass().getName());
    }

    /// @param name Name. Null gives the logger for the calling class.
    public static Logger getLogger(String name) {
        var loggerName = name == null ? caller() : name;
        return LOGGERS.computeIfAbsent(loggerName, _ -> new Log4jLogger(loggerName, Flogs.get(loggerName), false));
    }

    /// @return The formatter logger for the calling class
    public static Logger getFormatterLogger() {
        return getFormatterLogger(caller());
    }

    /// @param clazz Class. Null gives the formatter logger for the calling class.
    public static Logger getFormatterLogger(Class<?> clazz) {
        return getFormatterLogger(clazz == null ? caller() : clazz.getName());
    }

    /// @param value Object whose class names the logger. Null gives the formatter logger for the calling class.
    public static Logger getFormatterLogger(Object value) {
        return getFormatterLogger(value == null ? caller() : value.getClass().getName());
    }

    /// A formatter logger formats messages with [String#format(String, Object...)], and not with `{}`
    /// placeholders. A [Throwable] as the last parameter is logged with its stack trace.
    ///
    /// @param name Name. Null gives the formatter logger for the calling class.
    public static Logger getFormatterLogger(String name) {
        var loggerName = name == null ? caller() : name;
        return FORMATTER_LOGGERS.computeIfAbsent(
            loggerName,
            _ -> new Log4jLogger(loggerName, Flogs.get(loggerName), true)
        );
    }

    public static Logger getRootLogger() {
        return getLogger(ROOT_LOGGER_NAME);
    }

    public static boolean exists(String name) {
        return name != null && LOGGERS.containsKey(name);
    }

    /// Has no effect. Flogs writes each line when it is logged.
    public static void shutdown() {
    }

    /// Has no effect. Flogs writes each line when it is logged.
    public static void shutdown(boolean currentContext) {
    }

    /// Has no effect. Flogs writes each line when it is logged.
    public static void shutdown(boolean currentContext, boolean allContexts) {
    }

    private LogManager() {
    }

    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    private static final Map<String, Logger> FORMATTER_LOGGERS = new ConcurrentHashMap<>();

    private static final StackWalker WALKER = StackWalker.getInstance();

    private static final String THIS_CLASS = LogManager.class.getName();

    /// @return The name of the first class in the stack that is not this class
    private static String caller() {
        return WALKER.walk(frames -> frames
            .map(StackWalker.StackFrame::getClassName)
            .filter(name -> !name.equals(THIS_CLASS))
            .findFirst()
            .orElse(ROOT_LOGGER_NAME));
    }
}
