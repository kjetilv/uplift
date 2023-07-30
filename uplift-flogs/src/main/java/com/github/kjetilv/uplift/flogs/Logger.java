package com.github.kjetilv.uplift.flogs;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

@SuppressWarnings("unused")
public final class Logger {

    public enum Lev {
        ERROR(SEVERE),
        WARN(WARNING),
        INFO(Level.INFO),
        DEBUG(FINE),
        TRACE(FINEST);

        private final Level target;

        Lev(Level target) {
            this.target = target;
        }

        Level trg() {
            return target;
        }
    }

    @SuppressWarnings("NonConstantLogger")
    private final java.util.logging.Logger jul;

    private final String name;

    Logger(java.util.logging.Logger jul, String... knownPrefixes) {
        this.jul = requireNonNull(jul, "jul");
        String sourceName = requireNonNull(jul.getName(), "jul.getName()");
        int lastDot = sourceName.lastIndexOf('.');
        if (lastDot < 0) {
            this.name = sourceName;
        } else {
            this.name = shorten(sourceName);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isTraceEnabled() {
        return jul.isLoggable(FINEST);
    }

    public void trace(String msg) {
        log(FINEST, msg, (Throwable) null);
    }

    public void trace(String msg, Throwable t) {
        log(FINEST, msg, t);
    }

    public void trace(String format, Object arg) {
        log(FINEST, null, format, arg);
    }

    public void trace(String format, Object arg, Throwable t) {
        log(FINEST, t, format, arg);
    }

    public void trace(String format, Object arg1, Object arg2) {
        log(FINEST, format, null, arg1, arg2);
    }

    public void trace(String format, Object arg1, Object arg2, Throwable t) {
        log(FINEST, format, t, arg1, arg2);
    }

    public void trace(String format, Object arg1, Object arg2, Object arg3) {
        log(FINEST, format, null, arg1, arg2, arg3);
    }

    public void trace(String format, Object arg1, Object arg2, Object arg3, Throwable t) {
        log(FINEST, format, t, arg1, arg2, arg3);
    }

    public void trace(String format, Object... arguments) {
        log(FINEST, format, arguments);
    }

    public boolean isLogEnabled(Lev lev) {
        return jul.isLoggable(lev.trg());
    }

    public void log(Lev lev, String msg) {
        log(lev.trg(), msg, (Throwable) null);
    }

    public void log(Lev lev, String msg, Throwable t) {
        log(lev.trg(), msg, t);
    }

    public void log(Lev lev, String format, Object arg) {
        log(lev.trg(), null, format, arg);
    }

    public void log(Lev lev, String format, Object arg, Throwable t) {
        log(lev.trg(), t, format, arg);
    }

    public void log(Lev lev, String format, Object arg1, Object arg2) {
        log(lev.trg(), format, null, arg1, arg2);
    }

    public void log(Lev lev, String format, Object arg1, Object arg2, Throwable t) {
        log(lev.trg(), format, t, arg1, arg2);
    }

    public void log(Lev lev, String format, Object arg1, Object arg2, Object arg3) {
        log(lev.trg(), format, null, arg1, arg2, arg3);
    }

    public void log(Lev lev, String format, Object arg1, Object arg2, Object arg3, Throwable t) {
        log(lev.trg(), format, t, arg1, arg2, arg3);
    }

    public void log(Lev lev, String format, Object... arguments) {
        log(lev.trg(), format, arguments);
    }

    public boolean isDebugEnabled() {
        return jul.isLoggable(FINE);
    }

    public void debug(String msg) {
        log(FINE, msg, (Throwable) null);
    }

    public void debug(String msg, Throwable t) {
        log(FINE, msg, t);
    }

    public void debug(String format, Object arg) {
        log(FINE, null, format, arg);
    }

    public void debug(String format, Object arg, Throwable t) {
        log(FINE, t, format, arg);
    }

    public void debug(String format, Object arg1, Object arg2) {
        log(FINE, format, null, arg1, arg2);
    }

    public void debug(String format, Object arg1, Object arg2, Throwable t) {
        log(FINE, format, t, arg1, arg2);
    }

    public void debug(String format, Object arg1, Object arg2, Object arg3) {
        log(FINE, format, null, arg1, arg2, arg3);
    }

    public void debug(String format, Object arg1, Object arg2, Object arg3, Throwable t) {
        log(FINE, format, t, arg1, arg2, arg3);
    }

    public void debug(String format, Object... arguments) {
        log(FINE, format, arguments);
    }

    public boolean isInfoEnabled() {
        return jul.isLoggable(INFO);
    }

    public void info(String msg) {
        log(INFO, msg, (Throwable) null);
    }

    public void info(String format, Object arg) {
        log(INFO, null, format, arg);
    }

    public void info(String format, Object arg1, Object arg2) {
        log(INFO, format, null, arg1, arg2);
    }

    public void info(String format, Object arg1, Object arg2, Object arg3) {
        log(INFO, format, null, arg1, arg2, arg3);
    }

    public void info(String msg, Throwable t) {
        log(INFO, msg, t);
    }

    public void info(String format, Object arg, Throwable t) {
        log(INFO, t, format, arg);
    }

    public void info(String format, Object arg1, Object arg2, Throwable t) {
        log(INFO, format, t, arg1, arg2);
    }

    public void info(String format, Object arg1, Object arg2, Object arg3, Throwable t) {
        log(INFO, format, t, arg1, arg2, arg3);
    }

    public void info(String format, Object... arguments) {
        log(INFO, format, arguments);
    }

    public boolean isWarnEnabled() {
        return jul.isLoggable(WARNING);
    }

    public void warn(String msg) {
        log(WARNING, msg, (Throwable) null);
    }

    public void warn(String format, Object arg) {
        log(WARNING, null, format, arg);
    }

    public void warn(String format, Object arg1, Object arg2) {
        log(WARNING, format, null, arg1, arg2);
    }

    public void warn(String format, Object arg1, Object arg2, Object arg3) {
        log(WARNING, format, null, arg1, arg2, arg3);
    }

    public void warn(String msg, Throwable t) {
        log(WARNING, msg, t);
    }

    public void warn(String format, Object arg, Throwable t) {
        log(WARNING, t, format, arg);
    }

    public void warn(String format, Object arg1, Object arg2, Throwable t) {
        log(WARNING, format, t, arg1, arg2);
    }

    public void warn(String format, Object arg1, Object arg2, Object arg3, Throwable t) {
        log(WARNING, format, t, arg1, arg2, arg3);
    }

    public void warn(String format, Object... arguments) {
        log(WARNING, format, arguments);
    }

    public boolean isErrorEnabled() {
        return jul.isLoggable(SEVERE);
    }

    public void error(String msg) {
        log(SEVERE, msg, (Throwable) null);
    }

    public void error(String format, Object arg) {
        log(SEVERE, null, format, arg);
    }

    public void error(String format, Object arg1, Object arg2) {
        log(SEVERE, format, null, arg1, arg2);
    }

    public void error(String format, Object arg1, Object arg2, Object arg3) {
        log(SEVERE, format, null, arg1, arg2, arg3);
    }

    public void error(String msg, Throwable t) {
        log(SEVERE, msg, t);
    }

    public void error(String format, Object arg, Throwable t) {
        log(SEVERE, t, format, arg);
    }

    public void error(String format, Object arg1, Object arg2, Throwable t) {
        log(SEVERE, format, t, arg1, arg2);
    }

    public void error(String format, Object arg1, Object arg2, Object arg3, Throwable t) {
        log(SEVERE, format, t, arg1, arg2, arg3);
    }

    public void error(String format, Object... arguments) {
        log(SEVERE, format, arguments);
    }

    private void log(Level level, String msg, Throwable throwable) {
        if (jul.isLoggable(level)) {
            doLog(level, msg, throwable);
        }
    }

    private void log(Level level, Throwable t, String msg, Object arg) {
        if (jul.isLoggable(level)) {
            Throwable throwable = t != null ? t : arg instanceof Throwable ? (Throwable) arg : null;
            doLog(level, msg, throwable, new Object[] { arg });
        }
    }

    private void log(Level level, String msg, Throwable t, Object arg1, Object arg2) {
        if (jul.isLoggable(level)) {
            Throwable throwable = t != null ? t : arg2 instanceof Throwable ? (Throwable) arg2 : null;
            doLog(level, msg, throwable, new Object[] { arg1, arg2 });
        }
    }

    private void log(Level level, String msg, Throwable t, Object arg1, Object arg2, Object arg3) {
        if (jul.isLoggable(level)) {
            Throwable throwable = t != null ? t : arg3 instanceof Throwable ? (Throwable) arg3 : null;
            doLog(level, msg, throwable, new Object[] { arg1, arg2, arg3 });
        }
    }

    private void log(Level level, String msg, Object[] args) {
        if (jul.isLoggable(level)) {
            Object last = args != null && args.length > 0 ? args[args.length - 1] : null;
            Throwable throwable = last instanceof Throwable ? (Throwable) last : null;
            Object[] args2;
            if (throwable == null) {
                args2 = args;
            } else {
                args2 = new Object[args.length - 1];
                System.arraycopy(args, 0, args2, 0, args2.length);
            }
            doLog(level, msg, throwable, args2);
        }
    }

    private void doLog(Level level, String msg, Throwable throwable) {
        doLog(level, msg, throwable, null);
    }

    private void doLog(Level level, String msg, Throwable throwable, Object[] args) {
        LogRecord record = logRecord(name, level, msg, throwable, args);
        jul.log(record);
    }

    @SuppressWarnings("deprecation")
    private static LogRecord logRecord(String name, Level level, String msg, Throwable throwable, Object[] args) {
        LogRecord logRecord = new LogRecord(level, msg);
        logRecord.setLoggerName(name);
        logRecord.setInstant(Instant.now());
        logRecord.setThrown(throwable);
        Thread thread = Thread.currentThread();
        logRecord.setLongThreadID(thread.getId());
        if (args == null || args.length == 0) {
            return logRecord;
        }
        for (int i = 0; i < args.length; i++) {
            while (args[i] instanceof Supplier<?> supplier) {
                args[i] = supplier.get();
            }
        }
        logRecord.setParameters(args);
        return logRecord;
    }

    private static String shorten(String sourceName) {
        String[] split = sourceName.split("\\.");
        String className = split[split.length - 1];
        AtomicInteger count = new AtomicInteger(1);
        return Arrays.stream(split)
                   .limit(split.length - 1)
                   .map(p -> {
                       int len = p.length();
                       return len < count.get()
                           ? p
                           : p.substring(0, Math.min(len, count.incrementAndGet()));
                   })
                   .collect(Collectors.joining(".")) + "." + className;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jul.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Logger && Objects.equals(this.jul.getName(), ((Logger) obj).jul.getName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + "]";
    }
}
