package org.apache.logging.log4j;

import module java.base;
import com.github.kjetilv.uplift.flogs.Flogs;
import com.github.kjetilv.uplift.flogs.LogLevel;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.MessageSupplier;

/// Functions for the default methods of [Logger]
final class Log4jSupport {

    static final Object[] NO_PARAMS = {};

    /// @return The text of a message object. A [Message] gives its formatted message.
    static String text(Object message) {
        return message instanceof Message msg
            ? String.valueOf(msg.getFormattedMessage())
            : String.valueOf(message);
    }

    /// @return The throwable, or else the throwable of the message if it is a [Message]
    static Throwable throwable(Object message, Throwable throwable) {
        return throwable == null && message instanceof Message msg
            ? msg.getThrowable()
            : throwable;
    }

    /// @return The values of the suppliers. A [Message] value gives its formatted message.
    static Object[] values(java.util.function.Supplier<?>[] suppliers) {
        return suppliers == null
            ? NO_PARAMS
            : Arrays.stream(suppliers)
                .map(supplier -> supplier == null ? null : value(supplier.get()))
                .toArray();
    }

    static Throwable lastThrowable(Object[] params) {
        return params != null && params.length > 0 && params[params.length - 1] instanceof Throwable throwable
            ? throwable
            : null;
    }

    /// @return The formatted message, or the format and the parameters if the format is not valid
    static String printf(String format, Object[] params) {
        try {
            return String.format(String.valueOf(format), params);
        } catch (IllegalFormatException e) {
            return format + " " + Arrays.deepToString(params);
        }
    }

    /// Logs a warning the first time that the fluent API is used
    ///
    /// @return [LogBuilder#NOOP]
    static LogBuilder fluent() {
        if (!FLUENT_WARNED.get() && FLUENT_WARNED.compareAndSet(false, true)) {
            Flogs.get(LogBuilder.class).log(
                LogLevel.WARN,
                "Log4j fluent API used from {}: flogs does not support it, so lines logged with it are lost. " +
                "Later uses are not reported.",
                caller()
            );
        }
        return LogBuilder.NOOP;
    }

    private Log4jSupport() {
    }

    private static final AtomicBoolean FLUENT_WARNED = new AtomicBoolean();

    private static final String LOG4J_PACKAGE = Logger.class.getPackageName() + ".";

    private static final StackWalker WALKER = StackWalker.getInstance();

    /// @return The first class in the stack that is not in the Log4j package
    private static String caller() {
        return WALKER.walk(frames -> frames
            .map(StackWalker.StackFrame::getClassName)
            .filter(name -> !name.startsWith(LOG4J_PACKAGE))
            .findFirst()
            .orElse("unknown"));
    }

    private static Object value(Object value) {
        return switch (value) {
            case Message message -> message.getFormattedMessage();
            case MessageSupplier supplier -> text(supplier.get());
            case null, default -> value;
        };
    }
}
