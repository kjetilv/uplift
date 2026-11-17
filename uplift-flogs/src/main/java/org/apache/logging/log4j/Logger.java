package org.apache.logging.log4j;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;

import static org.apache.logging.log4j.Log4jSupport.NO_PARAMS;

/// The Log4j 2 logger API, without flow tracing (`traceEntry()` and others) and message factories. All
/// methods call [#isEnabled(Level, Marker)] and [#log(Level, Marker, String, Object...)], except the fluent
/// API (`atInfo()` and others). The fluent API returns [LogBuilder#NOOP], so it logs nothing. The first use
/// of the fluent API logs a warning.
///
/// Messages use `{}` placeholders. A [Throwable] as the last parameter is logged with its stack trace.
/// Suppliers are called only if the level is enabled. Markers do not change what is logged.
///
/// The overloads are generated from one table, so that code compiled against Log4j 2 finds each method it
/// calls.
@SuppressWarnings({"unused", "OverloadedVarargsMethod"})
public interface Logger {

    String getName();

    /// @return The level of flogs
    Level getLevel();

    boolean isEnabled(Level level, Marker marker);

    default boolean isEnabled(Level level) {
        return isEnabled(level, (Marker) null);
    }

    /// @return [LogBuilder#NOOP]. The first use of the fluent API logs a warning.
    default LogBuilder atTrace() {
        return Log4jSupport.fluent();
    }

    /// @return [LogBuilder#NOOP]. The first use of the fluent API logs a warning.
    default LogBuilder atDebug() {
        return Log4jSupport.fluent();
    }

    /// @return [LogBuilder#NOOP]. The first use of the fluent API logs a warning.
    default LogBuilder atInfo() {
        return Log4jSupport.fluent();
    }

    /// @return [LogBuilder#NOOP]. The first use of the fluent API logs a warning.
    default LogBuilder atWarn() {
        return Log4jSupport.fluent();
    }

    /// @return [LogBuilder#NOOP]. The first use of the fluent API logs a warning.
    default LogBuilder atError() {
        return Log4jSupport.fluent();
    }

    /// @return [LogBuilder#NOOP]. The first use of the fluent API logs a warning.
    default LogBuilder atFatal() {
        return Log4jSupport.fluent();
    }

    /// @return [LogBuilder#NOOP]. The first use of the fluent API logs a warning.
    default LogBuilder always() {
        return Log4jSupport.fluent();
    }

    /// @return [LogBuilder#NOOP]. The first use of the fluent API logs a warning.
    default LogBuilder atLevel(Level level) {
        return Log4jSupport.fluent();
    }

    default void printf(Level level, String format, Object... params) {
        printf(level, (Marker) null, format, params);
    }

    /// Formats the message with [String#format(String, Object...)]. A [Throwable] as the last parameter is
    /// logged with its stack trace.
    default void printf(Level level, Marker marker, String format, Object... params) {
        if (isEnabled(level, marker)) {
            var thrown = Log4jSupport.lastThrowable(params);
            log(
                level,
                marker,
                Log4jSupport.printf(format, params),
                thrown == null ? NO_PARAMS : new Object[] {thrown}
            );
        }
    }

    default void catching(Throwable throwable) {
        catching(Level.ERROR, throwable);
    }

    default void catching(Level level, Throwable throwable) {
        log(level, (Marker) null, "Catching", throwable);
    }

    default <T extends Throwable> T throwing(T throwable) {
        return throwing(Level.ERROR, throwable);
    }

    default <T extends Throwable> T throwing(Level level, T throwable) {
        log(level, (Marker) null, "Throwing", throwable);
        return throwable;
    }

    default void log(Level level, Marker marker, String message) {
        log(level, marker, message, NO_PARAMS);
    }

    void log(Level level, Marker marker, String message, Object... params);

    default void log(Level level, Marker marker, String message, Object p0) {
        log(level, marker, message, new Object[] {p0});
    }

    default void log(Level level, Marker marker, String message, Object p0, Object p1) {
        log(level, marker, message, new Object[] {p0, p1});
    }

    default void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        log(level, marker, message, new Object[] {p0, p1, p2});
    }

    default void log(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        log(level, marker, message, new Object[] {p0, p1, p2, p3});
    }

    default void log(
        Level level,
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4
    ) {
        log(level, marker, message, new Object[] {p0, p1, p2, p3, p4});
    }

    default void log(
        Level level,
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5
    ) {
        log(level, marker, message, new Object[] {p0, p1, p2, p3, p4, p5});
    }

    default void log(
        Level level,
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6
    ) {
        log(level, marker, message, new Object[] {p0, p1, p2, p3, p4, p5, p6});
    }

    default void log(
        Level level,
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(level, marker, message, new Object[] {p0, p1, p2, p3, p4, p5, p6, p7});
    }

    default void log(
        Level level,
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(level, marker, message, new Object[] {p0, p1, p2, p3, p4, p5, p6, p7, p8});
    }

    default void log(
        Level level,
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(level, marker, message, new Object[] {p0, p1, p2, p3, p4, p5, p6, p7, p8, p9});
    }

    default void log(Level level, Marker marker, String message, Throwable throwable) {
        log(level, marker, message, throwable == null ? NO_PARAMS : new Object[] {throwable});
    }

    default void log(Level level, Marker marker, Object message) {
        logObject(level, marker, message, null);
    }

    default void log(Level level, Marker marker, Object message, Throwable throwable) {
        logObject(level, marker, message, throwable);
    }

    default void log(Level level, Marker marker, CharSequence message) {
        logObject(level, marker, message, null);
    }

    default void log(Level level, Marker marker, CharSequence message, Throwable throwable) {
        logObject(level, marker, message, throwable);
    }

    default void log(Level level, Marker marker, Message message) {
        logObject(level, marker, message, null);
    }

    default void log(Level level, Marker marker, Message message, Throwable throwable) {
        logObject(level, marker, message, throwable);
    }

    default void log(Level level, Marker marker, Supplier<?> messageSupplier) {
        if (isEnabled(level, marker)) {
            logObject(level, marker, messageSupplier == null ? null : messageSupplier.get(), null);
        }
    }

    default void log(Level level, Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        if (isEnabled(level, marker)) {
            logObject(level, marker, messageSupplier == null ? null : messageSupplier.get(), throwable);
        }
    }

    default void log(Level level, Marker marker, String message, Supplier<?>... paramSuppliers) {
        if (isEnabled(level, marker)) {
            log(level, marker, message, values(paramSuppliers));
        }
    }

    default void log(Level level, Marker marker, MessageSupplier messageSupplier) {
        if (isEnabled(level, marker)) {
            logObject(level, marker, messageSupplier == null ? null : messageSupplier.get(), null);
        }
    }

    default void log(Level level, Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        if (isEnabled(level, marker)) {
            logObject(level, marker, messageSupplier == null ? null : messageSupplier.get(), throwable);
        }
    }

    default void log(Level level, String message) {
        log(level, (Marker) null, message);
    }

    default void log(Level level, String message, Object... params) {
        log(level, (Marker) null, message, params);
    }

    default void log(Level level, String message, Object p0) {
        log(level, (Marker) null, message, p0);
    }

    default void log(Level level, String message, Object p0, Object p1) {
        log(level, (Marker) null, message, p0, p1);
    }

    default void log(Level level, String message, Object p0, Object p1, Object p2) {
        log(level, (Marker) null, message, p0, p1, p2);
    }

    default void log(Level level, String message, Object p0, Object p1, Object p2, Object p3) {
        log(level, (Marker) null, message, p0, p1, p2, p3);
    }

    default void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(level, (Marker) null, message, p0, p1, p2, p3, p4);
    }

    default void log(Level level, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(level, (Marker) null, message, p0, p1, p2, p3, p4, p5);
    }

    default void log(
        Level level,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6
    ) {
        log(level, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void log(
        Level level,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(level, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void log(
        Level level,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(level, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void log(
        Level level,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(level, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void log(Level level, String message, Throwable throwable) {
        log(level, (Marker) null, message, throwable);
    }

    default void log(Level level, Object message) {
        log(level, (Marker) null, message);
    }

    default void log(Level level, Object message, Throwable throwable) {
        log(level, (Marker) null, message, throwable);
    }

    default void log(Level level, CharSequence message) {
        log(level, (Marker) null, message);
    }

    default void log(Level level, CharSequence message, Throwable throwable) {
        log(level, (Marker) null, message, throwable);
    }

    default void log(Level level, Message message) {
        log(level, (Marker) null, message);
    }

    default void log(Level level, Message message, Throwable throwable) {
        log(level, (Marker) null, message, throwable);
    }

    default void log(Level level, Supplier<?> messageSupplier) {
        log(level, (Marker) null, messageSupplier);
    }

    default void log(Level level, Supplier<?> messageSupplier, Throwable throwable) {
        log(level, (Marker) null, messageSupplier, throwable);
    }

    default void log(Level level, String message, Supplier<?>... paramSuppliers) {
        log(level, (Marker) null, message, paramSuppliers);
    }

    default void log(Level level, MessageSupplier messageSupplier) {
        log(level, (Marker) null, messageSupplier);
    }

    default void log(Level level, MessageSupplier messageSupplier, Throwable throwable) {
        log(level, (Marker) null, messageSupplier, throwable);
    }

    // TRACE

    default boolean isTraceEnabled() {
        return isEnabled(Level.TRACE, (Marker) null);
    }

    default boolean isTraceEnabled(Marker marker) {
        return isEnabled(Level.TRACE, marker);
    }

    default void trace(Marker marker, String message) {
        log(Level.TRACE, marker, message);
    }

    default void trace(Marker marker, String message, Object... params) {
        log(Level.TRACE, marker, message, params);
    }

    default void trace(Marker marker, String message, Object p0) {
        log(Level.TRACE, marker, message, p0);
    }

    default void trace(Marker marker, String message, Object p0, Object p1) {
        log(Level.TRACE, marker, message, p0, p1);
    }

    default void trace(Marker marker, String message, Object p0, Object p1, Object p2) {
        log(Level.TRACE, marker, message, p0, p1, p2);
    }

    default void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.TRACE, marker, message, p0, p1, p2, p3);
    }

    default void trace(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.TRACE, marker, message, p0, p1, p2, p3, p4);
    }

    default void trace(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5
    ) {
        log(Level.TRACE, marker, message, p0, p1, p2, p3, p4, p5);
    }

    default void trace(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6
    ) {
        log(Level.TRACE, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void trace(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.TRACE, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void trace(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.TRACE, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void trace(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.TRACE, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void trace(Marker marker, String message, Throwable throwable) {
        log(Level.TRACE, marker, message, throwable);
    }

    default void trace(Marker marker, Object message) {
        log(Level.TRACE, marker, message);
    }

    default void trace(Marker marker, Object message, Throwable throwable) {
        log(Level.TRACE, marker, message, throwable);
    }

    default void trace(Marker marker, CharSequence message) {
        log(Level.TRACE, marker, message);
    }

    default void trace(Marker marker, CharSequence message, Throwable throwable) {
        log(Level.TRACE, marker, message, throwable);
    }

    default void trace(Marker marker, Message message) {
        log(Level.TRACE, marker, message);
    }

    default void trace(Marker marker, Message message, Throwable throwable) {
        log(Level.TRACE, marker, message, throwable);
    }

    default void trace(Marker marker, Supplier<?> messageSupplier) {
        log(Level.TRACE, marker, messageSupplier);
    }

    default void trace(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.TRACE, marker, messageSupplier, throwable);
    }

    default void trace(Marker marker, String message, Supplier<?>... paramSuppliers) {
        log(Level.TRACE, marker, message, paramSuppliers);
    }

    default void trace(Marker marker, MessageSupplier messageSupplier) {
        log(Level.TRACE, marker, messageSupplier);
    }

    default void trace(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.TRACE, marker, messageSupplier, throwable);
    }

    default void trace(String message) {
        log(Level.TRACE, (Marker) null, message);
    }

    default void trace(String message, Object... params) {
        log(Level.TRACE, (Marker) null, message, params);
    }

    default void trace(String message, Object p0) {
        log(Level.TRACE, (Marker) null, message, p0);
    }

    default void trace(String message, Object p0, Object p1) {
        log(Level.TRACE, (Marker) null, message, p0, p1);
    }

    default void trace(String message, Object p0, Object p1, Object p2) {
        log(Level.TRACE, (Marker) null, message, p0, p1, p2);
    }

    default void trace(String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.TRACE, (Marker) null, message, p0, p1, p2, p3);
    }

    default void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.TRACE, (Marker) null, message, p0, p1, p2, p3, p4);
    }

    default void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(Level.TRACE, (Marker) null, message, p0, p1, p2, p3, p4, p5);
    }

    default void trace(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        log(Level.TRACE, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void trace(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.TRACE, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void trace(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.TRACE, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void trace(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.TRACE, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void trace(String message, Throwable throwable) {
        log(Level.TRACE, (Marker) null, message, throwable);
    }

    default void trace(Object message) {
        log(Level.TRACE, (Marker) null, message);
    }

    default void trace(Object message, Throwable throwable) {
        log(Level.TRACE, (Marker) null, message, throwable);
    }

    default void trace(CharSequence message) {
        log(Level.TRACE, (Marker) null, message);
    }

    default void trace(CharSequence message, Throwable throwable) {
        log(Level.TRACE, (Marker) null, message, throwable);
    }

    default void trace(Message message) {
        log(Level.TRACE, (Marker) null, message);
    }

    default void trace(Message message, Throwable throwable) {
        log(Level.TRACE, (Marker) null, message, throwable);
    }

    default void trace(Supplier<?> messageSupplier) {
        log(Level.TRACE, (Marker) null, messageSupplier);
    }

    default void trace(Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.TRACE, (Marker) null, messageSupplier, throwable);
    }

    default void trace(String message, Supplier<?>... paramSuppliers) {
        log(Level.TRACE, (Marker) null, message, paramSuppliers);
    }

    default void trace(MessageSupplier messageSupplier) {
        log(Level.TRACE, (Marker) null, messageSupplier);
    }

    default void trace(MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.TRACE, (Marker) null, messageSupplier, throwable);
    }

    // DEBUG

    default boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG, (Marker) null);
    }

    default boolean isDebugEnabled(Marker marker) {
        return isEnabled(Level.DEBUG, marker);
    }

    default void debug(Marker marker, String message) {
        log(Level.DEBUG, marker, message);
    }

    default void debug(Marker marker, String message, Object... params) {
        log(Level.DEBUG, marker, message, params);
    }

    default void debug(Marker marker, String message, Object p0) {
        log(Level.DEBUG, marker, message, p0);
    }

    default void debug(Marker marker, String message, Object p0, Object p1) {
        log(Level.DEBUG, marker, message, p0, p1);
    }

    default void debug(Marker marker, String message, Object p0, Object p1, Object p2) {
        log(Level.DEBUG, marker, message, p0, p1, p2);
    }

    default void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.DEBUG, marker, message, p0, p1, p2, p3);
    }

    default void debug(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.DEBUG, marker, message, p0, p1, p2, p3, p4);
    }

    default void debug(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5
    ) {
        log(Level.DEBUG, marker, message, p0, p1, p2, p3, p4, p5);
    }

    default void debug(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6
    ) {
        log(Level.DEBUG, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void debug(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.DEBUG, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void debug(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.DEBUG, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void debug(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.DEBUG, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void debug(Marker marker, String message, Throwable throwable) {
        log(Level.DEBUG, marker, message, throwable);
    }

    default void debug(Marker marker, Object message) {
        log(Level.DEBUG, marker, message);
    }

    default void debug(Marker marker, Object message, Throwable throwable) {
        log(Level.DEBUG, marker, message, throwable);
    }

    default void debug(Marker marker, CharSequence message) {
        log(Level.DEBUG, marker, message);
    }

    default void debug(Marker marker, CharSequence message, Throwable throwable) {
        log(Level.DEBUG, marker, message, throwable);
    }

    default void debug(Marker marker, Message message) {
        log(Level.DEBUG, marker, message);
    }

    default void debug(Marker marker, Message message, Throwable throwable) {
        log(Level.DEBUG, marker, message, throwable);
    }

    default void debug(Marker marker, Supplier<?> messageSupplier) {
        log(Level.DEBUG, marker, messageSupplier);
    }

    default void debug(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.DEBUG, marker, messageSupplier, throwable);
    }

    default void debug(Marker marker, String message, Supplier<?>... paramSuppliers) {
        log(Level.DEBUG, marker, message, paramSuppliers);
    }

    default void debug(Marker marker, MessageSupplier messageSupplier) {
        log(Level.DEBUG, marker, messageSupplier);
    }

    default void debug(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.DEBUG, marker, messageSupplier, throwable);
    }

    default void debug(String message) {
        log(Level.DEBUG, (Marker) null, message);
    }

    default void debug(String message, Object... params) {
        log(Level.DEBUG, (Marker) null, message, params);
    }

    default void debug(String message, Object p0) {
        log(Level.DEBUG, (Marker) null, message, p0);
    }

    default void debug(String message, Object p0, Object p1) {
        log(Level.DEBUG, (Marker) null, message, p0, p1);
    }

    default void debug(String message, Object p0, Object p1, Object p2) {
        log(Level.DEBUG, (Marker) null, message, p0, p1, p2);
    }

    default void debug(String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.DEBUG, (Marker) null, message, p0, p1, p2, p3);
    }

    default void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.DEBUG, (Marker) null, message, p0, p1, p2, p3, p4);
    }

    default void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(Level.DEBUG, (Marker) null, message, p0, p1, p2, p3, p4, p5);
    }

    default void debug(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        log(Level.DEBUG, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void debug(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.DEBUG, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void debug(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.DEBUG, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void debug(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.DEBUG, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void debug(String message, Throwable throwable) {
        log(Level.DEBUG, (Marker) null, message, throwable);
    }

    default void debug(Object message) {
        log(Level.DEBUG, (Marker) null, message);
    }

    default void debug(Object message, Throwable throwable) {
        log(Level.DEBUG, (Marker) null, message, throwable);
    }

    default void debug(CharSequence message) {
        log(Level.DEBUG, (Marker) null, message);
    }

    default void debug(CharSequence message, Throwable throwable) {
        log(Level.DEBUG, (Marker) null, message, throwable);
    }

    default void debug(Message message) {
        log(Level.DEBUG, (Marker) null, message);
    }

    default void debug(Message message, Throwable throwable) {
        log(Level.DEBUG, (Marker) null, message, throwable);
    }

    default void debug(Supplier<?> messageSupplier) {
        log(Level.DEBUG, (Marker) null, messageSupplier);
    }

    default void debug(Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.DEBUG, (Marker) null, messageSupplier, throwable);
    }

    default void debug(String message, Supplier<?>... paramSuppliers) {
        log(Level.DEBUG, (Marker) null, message, paramSuppliers);
    }

    default void debug(MessageSupplier messageSupplier) {
        log(Level.DEBUG, (Marker) null, messageSupplier);
    }

    default void debug(MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.DEBUG, (Marker) null, messageSupplier, throwable);
    }

    // INFO

    default boolean isInfoEnabled() {
        return isEnabled(Level.INFO, (Marker) null);
    }

    default boolean isInfoEnabled(Marker marker) {
        return isEnabled(Level.INFO, marker);
    }

    default void info(Marker marker, String message) {
        log(Level.INFO, marker, message);
    }

    default void info(Marker marker, String message, Object... params) {
        log(Level.INFO, marker, message, params);
    }

    default void info(Marker marker, String message, Object p0) {
        log(Level.INFO, marker, message, p0);
    }

    default void info(Marker marker, String message, Object p0, Object p1) {
        log(Level.INFO, marker, message, p0, p1);
    }

    default void info(Marker marker, String message, Object p0, Object p1, Object p2) {
        log(Level.INFO, marker, message, p0, p1, p2);
    }

    default void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.INFO, marker, message, p0, p1, p2, p3);
    }

    default void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.INFO, marker, message, p0, p1, p2, p3, p4);
    }

    default void info(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(Level.INFO, marker, message, p0, p1, p2, p3, p4, p5);
    }

    default void info(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6
    ) {
        log(Level.INFO, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void info(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.INFO, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void info(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.INFO, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void info(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.INFO, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void info(Marker marker, String message, Throwable throwable) {
        log(Level.INFO, marker, message, throwable);
    }

    default void info(Marker marker, Object message) {
        log(Level.INFO, marker, message);
    }

    default void info(Marker marker, Object message, Throwable throwable) {
        log(Level.INFO, marker, message, throwable);
    }

    default void info(Marker marker, CharSequence message) {
        log(Level.INFO, marker, message);
    }

    default void info(Marker marker, CharSequence message, Throwable throwable) {
        log(Level.INFO, marker, message, throwable);
    }

    default void info(Marker marker, Message message) {
        log(Level.INFO, marker, message);
    }

    default void info(Marker marker, Message message, Throwable throwable) {
        log(Level.INFO, marker, message, throwable);
    }

    default void info(Marker marker, Supplier<?> messageSupplier) {
        log(Level.INFO, marker, messageSupplier);
    }

    default void info(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.INFO, marker, messageSupplier, throwable);
    }

    default void info(Marker marker, String message, Supplier<?>... paramSuppliers) {
        log(Level.INFO, marker, message, paramSuppliers);
    }

    default void info(Marker marker, MessageSupplier messageSupplier) {
        log(Level.INFO, marker, messageSupplier);
    }

    default void info(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.INFO, marker, messageSupplier, throwable);
    }

    default void info(String message) {
        log(Level.INFO, (Marker) null, message);
    }

    default void info(String message, Object... params) {
        log(Level.INFO, (Marker) null, message, params);
    }

    default void info(String message, Object p0) {
        log(Level.INFO, (Marker) null, message, p0);
    }

    default void info(String message, Object p0, Object p1) {
        log(Level.INFO, (Marker) null, message, p0, p1);
    }

    default void info(String message, Object p0, Object p1, Object p2) {
        log(Level.INFO, (Marker) null, message, p0, p1, p2);
    }

    default void info(String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.INFO, (Marker) null, message, p0, p1, p2, p3);
    }

    default void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.INFO, (Marker) null, message, p0, p1, p2, p3, p4);
    }

    default void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(Level.INFO, (Marker) null, message, p0, p1, p2, p3, p4, p5);
    }

    default void info(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        log(Level.INFO, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void info(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.INFO, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void info(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.INFO, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void info(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.INFO, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void info(String message, Throwable throwable) {
        log(Level.INFO, (Marker) null, message, throwable);
    }

    default void info(Object message) {
        log(Level.INFO, (Marker) null, message);
    }

    default void info(Object message, Throwable throwable) {
        log(Level.INFO, (Marker) null, message, throwable);
    }

    default void info(CharSequence message) {
        log(Level.INFO, (Marker) null, message);
    }

    default void info(CharSequence message, Throwable throwable) {
        log(Level.INFO, (Marker) null, message, throwable);
    }

    default void info(Message message) {
        log(Level.INFO, (Marker) null, message);
    }

    default void info(Message message, Throwable throwable) {
        log(Level.INFO, (Marker) null, message, throwable);
    }

    default void info(Supplier<?> messageSupplier) {
        log(Level.INFO, (Marker) null, messageSupplier);
    }

    default void info(Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.INFO, (Marker) null, messageSupplier, throwable);
    }

    default void info(String message, Supplier<?>... paramSuppliers) {
        log(Level.INFO, (Marker) null, message, paramSuppliers);
    }

    default void info(MessageSupplier messageSupplier) {
        log(Level.INFO, (Marker) null, messageSupplier);
    }

    default void info(MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.INFO, (Marker) null, messageSupplier, throwable);
    }

    // WARN

    default boolean isWarnEnabled() {
        return isEnabled(Level.WARN, (Marker) null);
    }

    default boolean isWarnEnabled(Marker marker) {
        return isEnabled(Level.WARN, marker);
    }

    default void warn(Marker marker, String message) {
        log(Level.WARN, marker, message);
    }

    default void warn(Marker marker, String message, Object... params) {
        log(Level.WARN, marker, message, params);
    }

    default void warn(Marker marker, String message, Object p0) {
        log(Level.WARN, marker, message, p0);
    }

    default void warn(Marker marker, String message, Object p0, Object p1) {
        log(Level.WARN, marker, message, p0, p1);
    }

    default void warn(Marker marker, String message, Object p0, Object p1, Object p2) {
        log(Level.WARN, marker, message, p0, p1, p2);
    }

    default void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.WARN, marker, message, p0, p1, p2, p3);
    }

    default void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.WARN, marker, message, p0, p1, p2, p3, p4);
    }

    default void warn(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(Level.WARN, marker, message, p0, p1, p2, p3, p4, p5);
    }

    default void warn(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6
    ) {
        log(Level.WARN, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void warn(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.WARN, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void warn(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.WARN, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void warn(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.WARN, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void warn(Marker marker, String message, Throwable throwable) {
        log(Level.WARN, marker, message, throwable);
    }

    default void warn(Marker marker, Object message) {
        log(Level.WARN, marker, message);
    }

    default void warn(Marker marker, Object message, Throwable throwable) {
        log(Level.WARN, marker, message, throwable);
    }

    default void warn(Marker marker, CharSequence message) {
        log(Level.WARN, marker, message);
    }

    default void warn(Marker marker, CharSequence message, Throwable throwable) {
        log(Level.WARN, marker, message, throwable);
    }

    default void warn(Marker marker, Message message) {
        log(Level.WARN, marker, message);
    }

    default void warn(Marker marker, Message message, Throwable throwable) {
        log(Level.WARN, marker, message, throwable);
    }

    default void warn(Marker marker, Supplier<?> messageSupplier) {
        log(Level.WARN, marker, messageSupplier);
    }

    default void warn(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.WARN, marker, messageSupplier, throwable);
    }

    default void warn(Marker marker, String message, Supplier<?>... paramSuppliers) {
        log(Level.WARN, marker, message, paramSuppliers);
    }

    default void warn(Marker marker, MessageSupplier messageSupplier) {
        log(Level.WARN, marker, messageSupplier);
    }

    default void warn(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.WARN, marker, messageSupplier, throwable);
    }

    default void warn(String message) {
        log(Level.WARN, (Marker) null, message);
    }

    default void warn(String message, Object... params) {
        log(Level.WARN, (Marker) null, message, params);
    }

    default void warn(String message, Object p0) {
        log(Level.WARN, (Marker) null, message, p0);
    }

    default void warn(String message, Object p0, Object p1) {
        log(Level.WARN, (Marker) null, message, p0, p1);
    }

    default void warn(String message, Object p0, Object p1, Object p2) {
        log(Level.WARN, (Marker) null, message, p0, p1, p2);
    }

    default void warn(String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.WARN, (Marker) null, message, p0, p1, p2, p3);
    }

    default void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.WARN, (Marker) null, message, p0, p1, p2, p3, p4);
    }

    default void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(Level.WARN, (Marker) null, message, p0, p1, p2, p3, p4, p5);
    }

    default void warn(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        log(Level.WARN, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void warn(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.WARN, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void warn(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.WARN, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void warn(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.WARN, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void warn(String message, Throwable throwable) {
        log(Level.WARN, (Marker) null, message, throwable);
    }

    default void warn(Object message) {
        log(Level.WARN, (Marker) null, message);
    }

    default void warn(Object message, Throwable throwable) {
        log(Level.WARN, (Marker) null, message, throwable);
    }

    default void warn(CharSequence message) {
        log(Level.WARN, (Marker) null, message);
    }

    default void warn(CharSequence message, Throwable throwable) {
        log(Level.WARN, (Marker) null, message, throwable);
    }

    default void warn(Message message) {
        log(Level.WARN, (Marker) null, message);
    }

    default void warn(Message message, Throwable throwable) {
        log(Level.WARN, (Marker) null, message, throwable);
    }

    default void warn(Supplier<?> messageSupplier) {
        log(Level.WARN, (Marker) null, messageSupplier);
    }

    default void warn(Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.WARN, (Marker) null, messageSupplier, throwable);
    }

    default void warn(String message, Supplier<?>... paramSuppliers) {
        log(Level.WARN, (Marker) null, message, paramSuppliers);
    }

    default void warn(MessageSupplier messageSupplier) {
        log(Level.WARN, (Marker) null, messageSupplier);
    }

    default void warn(MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.WARN, (Marker) null, messageSupplier, throwable);
    }

    // ERROR

    default boolean isErrorEnabled() {
        return isEnabled(Level.ERROR, (Marker) null);
    }

    default boolean isErrorEnabled(Marker marker) {
        return isEnabled(Level.ERROR, marker);
    }

    default void error(Marker marker, String message) {
        log(Level.ERROR, marker, message);
    }

    default void error(Marker marker, String message, Object... params) {
        log(Level.ERROR, marker, message, params);
    }

    default void error(Marker marker, String message, Object p0) {
        log(Level.ERROR, marker, message, p0);
    }

    default void error(Marker marker, String message, Object p0, Object p1) {
        log(Level.ERROR, marker, message, p0, p1);
    }

    default void error(Marker marker, String message, Object p0, Object p1, Object p2) {
        log(Level.ERROR, marker, message, p0, p1, p2);
    }

    default void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.ERROR, marker, message, p0, p1, p2, p3);
    }

    default void error(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.ERROR, marker, message, p0, p1, p2, p3, p4);
    }

    default void error(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5
    ) {
        log(Level.ERROR, marker, message, p0, p1, p2, p3, p4, p5);
    }

    default void error(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6
    ) {
        log(Level.ERROR, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void error(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.ERROR, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void error(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.ERROR, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void error(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.ERROR, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void error(Marker marker, String message, Throwable throwable) {
        log(Level.ERROR, marker, message, throwable);
    }

    default void error(Marker marker, Object message) {
        log(Level.ERROR, marker, message);
    }

    default void error(Marker marker, Object message, Throwable throwable) {
        log(Level.ERROR, marker, message, throwable);
    }

    default void error(Marker marker, CharSequence message) {
        log(Level.ERROR, marker, message);
    }

    default void error(Marker marker, CharSequence message, Throwable throwable) {
        log(Level.ERROR, marker, message, throwable);
    }

    default void error(Marker marker, Message message) {
        log(Level.ERROR, marker, message);
    }

    default void error(Marker marker, Message message, Throwable throwable) {
        log(Level.ERROR, marker, message, throwable);
    }

    default void error(Marker marker, Supplier<?> messageSupplier) {
        log(Level.ERROR, marker, messageSupplier);
    }

    default void error(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.ERROR, marker, messageSupplier, throwable);
    }

    default void error(Marker marker, String message, Supplier<?>... paramSuppliers) {
        log(Level.ERROR, marker, message, paramSuppliers);
    }

    default void error(Marker marker, MessageSupplier messageSupplier) {
        log(Level.ERROR, marker, messageSupplier);
    }

    default void error(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.ERROR, marker, messageSupplier, throwable);
    }

    default void error(String message) {
        log(Level.ERROR, (Marker) null, message);
    }

    default void error(String message, Object... params) {
        log(Level.ERROR, (Marker) null, message, params);
    }

    default void error(String message, Object p0) {
        log(Level.ERROR, (Marker) null, message, p0);
    }

    default void error(String message, Object p0, Object p1) {
        log(Level.ERROR, (Marker) null, message, p0, p1);
    }

    default void error(String message, Object p0, Object p1, Object p2) {
        log(Level.ERROR, (Marker) null, message, p0, p1, p2);
    }

    default void error(String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.ERROR, (Marker) null, message, p0, p1, p2, p3);
    }

    default void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.ERROR, (Marker) null, message, p0, p1, p2, p3, p4);
    }

    default void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(Level.ERROR, (Marker) null, message, p0, p1, p2, p3, p4, p5);
    }

    default void error(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        log(Level.ERROR, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void error(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.ERROR, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void error(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.ERROR, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void error(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.ERROR, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void error(String message, Throwable throwable) {
        log(Level.ERROR, (Marker) null, message, throwable);
    }

    default void error(Object message) {
        log(Level.ERROR, (Marker) null, message);
    }

    default void error(Object message, Throwable throwable) {
        log(Level.ERROR, (Marker) null, message, throwable);
    }

    default void error(CharSequence message) {
        log(Level.ERROR, (Marker) null, message);
    }

    default void error(CharSequence message, Throwable throwable) {
        log(Level.ERROR, (Marker) null, message, throwable);
    }

    default void error(Message message) {
        log(Level.ERROR, (Marker) null, message);
    }

    default void error(Message message, Throwable throwable) {
        log(Level.ERROR, (Marker) null, message, throwable);
    }

    default void error(Supplier<?> messageSupplier) {
        log(Level.ERROR, (Marker) null, messageSupplier);
    }

    default void error(Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.ERROR, (Marker) null, messageSupplier, throwable);
    }

    default void error(String message, Supplier<?>... paramSuppliers) {
        log(Level.ERROR, (Marker) null, message, paramSuppliers);
    }

    default void error(MessageSupplier messageSupplier) {
        log(Level.ERROR, (Marker) null, messageSupplier);
    }

    default void error(MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.ERROR, (Marker) null, messageSupplier, throwable);
    }

    // FATAL

    default boolean isFatalEnabled() {
        return isEnabled(Level.FATAL, (Marker) null);
    }

    default boolean isFatalEnabled(Marker marker) {
        return isEnabled(Level.FATAL, marker);
    }

    default void fatal(Marker marker, String message) {
        log(Level.FATAL, marker, message);
    }

    default void fatal(Marker marker, String message, Object... params) {
        log(Level.FATAL, marker, message, params);
    }

    default void fatal(Marker marker, String message, Object p0) {
        log(Level.FATAL, marker, message, p0);
    }

    default void fatal(Marker marker, String message, Object p0, Object p1) {
        log(Level.FATAL, marker, message, p0, p1);
    }

    default void fatal(Marker marker, String message, Object p0, Object p1, Object p2) {
        log(Level.FATAL, marker, message, p0, p1, p2);
    }

    default void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.FATAL, marker, message, p0, p1, p2, p3);
    }

    default void fatal(Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.FATAL, marker, message, p0, p1, p2, p3, p4);
    }

    default void fatal(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5
    ) {
        log(Level.FATAL, marker, message, p0, p1, p2, p3, p4, p5);
    }

    default void fatal(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6
    ) {
        log(Level.FATAL, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void fatal(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.FATAL, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void fatal(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.FATAL, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void fatal(
        Marker marker,
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.FATAL, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void fatal(Marker marker, String message, Throwable throwable) {
        log(Level.FATAL, marker, message, throwable);
    }

    default void fatal(Marker marker, Object message) {
        log(Level.FATAL, marker, message);
    }

    default void fatal(Marker marker, Object message, Throwable throwable) {
        log(Level.FATAL, marker, message, throwable);
    }

    default void fatal(Marker marker, CharSequence message) {
        log(Level.FATAL, marker, message);
    }

    default void fatal(Marker marker, CharSequence message, Throwable throwable) {
        log(Level.FATAL, marker, message, throwable);
    }

    default void fatal(Marker marker, Message message) {
        log(Level.FATAL, marker, message);
    }

    default void fatal(Marker marker, Message message, Throwable throwable) {
        log(Level.FATAL, marker, message, throwable);
    }

    default void fatal(Marker marker, Supplier<?> messageSupplier) {
        log(Level.FATAL, marker, messageSupplier);
    }

    default void fatal(Marker marker, Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.FATAL, marker, messageSupplier, throwable);
    }

    default void fatal(Marker marker, String message, Supplier<?>... paramSuppliers) {
        log(Level.FATAL, marker, message, paramSuppliers);
    }

    default void fatal(Marker marker, MessageSupplier messageSupplier) {
        log(Level.FATAL, marker, messageSupplier);
    }

    default void fatal(Marker marker, MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.FATAL, marker, messageSupplier, throwable);
    }

    default void fatal(String message) {
        log(Level.FATAL, (Marker) null, message);
    }

    default void fatal(String message, Object... params) {
        log(Level.FATAL, (Marker) null, message, params);
    }

    default void fatal(String message, Object p0) {
        log(Level.FATAL, (Marker) null, message, p0);
    }

    default void fatal(String message, Object p0, Object p1) {
        log(Level.FATAL, (Marker) null, message, p0, p1);
    }

    default void fatal(String message, Object p0, Object p1, Object p2) {
        log(Level.FATAL, (Marker) null, message, p0, p1, p2);
    }

    default void fatal(String message, Object p0, Object p1, Object p2, Object p3) {
        log(Level.FATAL, (Marker) null, message, p0, p1, p2, p3);
    }

    default void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        log(Level.FATAL, (Marker) null, message, p0, p1, p2, p3, p4);
    }

    default void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        log(Level.FATAL, (Marker) null, message, p0, p1, p2, p3, p4, p5);
    }

    default void fatal(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        log(Level.FATAL, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    default void fatal(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7
    ) {
        log(Level.FATAL, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    default void fatal(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8
    ) {
        log(Level.FATAL, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    default void fatal(
        String message,
        Object p0,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9
    ) {
        log(Level.FATAL, (Marker) null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    default void fatal(String message, Throwable throwable) {
        log(Level.FATAL, (Marker) null, message, throwable);
    }

    default void fatal(Object message) {
        log(Level.FATAL, (Marker) null, message);
    }

    default void fatal(Object message, Throwable throwable) {
        log(Level.FATAL, (Marker) null, message, throwable);
    }

    default void fatal(CharSequence message) {
        log(Level.FATAL, (Marker) null, message);
    }

    default void fatal(CharSequence message, Throwable throwable) {
        log(Level.FATAL, (Marker) null, message, throwable);
    }

    default void fatal(Message message) {
        log(Level.FATAL, (Marker) null, message);
    }

    default void fatal(Message message, Throwable throwable) {
        log(Level.FATAL, (Marker) null, message, throwable);
    }

    default void fatal(Supplier<?> messageSupplier) {
        log(Level.FATAL, (Marker) null, messageSupplier);
    }

    default void fatal(Supplier<?> messageSupplier, Throwable throwable) {
        log(Level.FATAL, (Marker) null, messageSupplier, throwable);
    }

    default void fatal(String message, Supplier<?>... paramSuppliers) {
        log(Level.FATAL, (Marker) null, message, paramSuppliers);
    }

    default void fatal(MessageSupplier messageSupplier) {
        log(Level.FATAL, (Marker) null, messageSupplier);
    }

    default void fatal(MessageSupplier messageSupplier, Throwable throwable) {
        log(Level.FATAL, (Marker) null, messageSupplier, throwable);
    }

    /// Logs the text of a message object. The text is not formatted again, so `{}` in it stays as it is.
    private void logObject(Level level, Marker marker, Object message, Throwable throwable) {
        if (isEnabled(level, marker)) {
            var thrown = Log4jSupport.throwable(message, throwable);
            log(level, marker, Log4jSupport.text(message), thrown == null ? NO_PARAMS : new Object[] {thrown});
        }
    }

    private static Object[] values(Supplier<?>... paramSuppliers) {
        return Log4jSupport.values(paramSuppliers);
    }
}
