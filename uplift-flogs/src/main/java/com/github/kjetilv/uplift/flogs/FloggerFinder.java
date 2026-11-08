package com.github.kjetilv.uplift.flogs;

import module java.base;

/// Makes flogs the backend of [System.Logger], so that the JDK's own logging comes out in the flogs format.
///
/// Messages follow the [System.Logger] contract: a message is looked up in the resource bundle, if one is
/// given, and parameters are substituted with [MessageFormat]. As in [java.util.logging.Formatter], the
/// message is only formatted if it refers to a parameter, such as `{0}`. The formatted message is passed to
/// flogs without parameters, so flogs' `{}` placeholders do not apply to it.
public class FloggerFinder extends System.LoggerFinder {

    private final Function<String, Logger> floggers;

    private final Map<String, System.Logger> loggers = new ConcurrentHashMap<>();

    /// Used by the service loader. Loggers come from [Flogs#get(String)].
    public FloggerFinder() {
        this(null);
    }

    /// @param floggers Creates the flogs logger for a name
    public FloggerFinder(Function<String, Logger> floggers) {
        this.floggers = floggers == null ? Flogs::get : floggers;
    }

    @Override
    public System.Logger getLogger(String name, Module module) {
        return loggers.computeIfAbsent(name, _ -> new SystemFlogger(name, floggers.apply(name)));
    }

    private static LogLevel translate(System.Logger.Level level) {
        return switch (level) {
            case ALL, TRACE -> LogLevel.TRACE;
            case DEBUG -> LogLevel.DEBUG;
            case INFO -> LogLevel.INFO;
            case WARNING -> LogLevel.WARN;
            case ERROR -> LogLevel.ERROR;
            case OFF -> LogLevel.NONE;
        };
    }

    /// @return The message from the bundle, or the key itself if there is no bundle or no such message
    private static String localized(ResourceBundle bundle, String key) {
        if (bundle == null || key == null) {
            return key;
        }
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /// @return The message with parameters substituted, or the message as it is if it has no parameter
    ///     references or is not a valid [MessageFormat] pattern
    private static String formatted(String message, Object[] params) {
        if (message == null || params == null || params.length == 0 || !refersToParameter(message)) {
            return message;
        }
        try {
            return MessageFormat.format(message, params);
        } catch (IllegalArgumentException e) {
            return message;
        }
    }

    /// @return Whether the message has a `{` followed by a digit
    private static boolean refersToParameter(String message) {
        for (var i = message.indexOf('{'); i >= 0 && i < message.length() - 1; i = message.indexOf('{', i + 1)) {
            if (Character.isDigit(message.charAt(i + 1))) {
                return true;
            }
        }
        return false;
    }

    private record SystemFlogger(String name, Logger flogger) implements System.Logger {

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isLoggable(Level level) {
            return level != Level.OFF && flogger.isEnabled(translate(level));
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
            if (isLoggable(level)) {
                var message = localized(bundle, msg);
                if (thrown == null) {
                    flogger.log(translate(level), message);
                } else {
                    flogger.log(translate(level), message, thrown);
                }
            }
        }

        @Override
        public void log(Level level, ResourceBundle bundle, String format, Object... params) {
            if (isLoggable(level)) {
                flogger.log(translate(level), formatted(localized(bundle, format), params));
            }
        }
    }
}
