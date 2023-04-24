package com.github.kjetilv.uplift.flogs;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.MILLIS;

public final class DefaultLogFormatter extends SimpleFormatter {

    public static void set(Supplier<DefaultLogFormatter> defaultLogFormatter) {
        INSTANCE.updateAndGet(existing ->
            existing == null
                ? defaultLogFormatter.get()
                : existing);
    }

    static Optional<DefaultLogFormatter> get() {
        return Optional.ofNullable(INSTANCE.get());
    }

    private final Function<Long, Optional<String>> threadNamer;

    DefaultLogFormatter() {
        this(null);
    }

    public DefaultLogFormatter(Function<Long, Optional<String>> threadNamer) {
        this.threadNamer = threadNamer;
    }

    @Override
    public String format(LogRecord record) {
        try {
            String line = line(record);
            Throwable e = record.getThrown();
            return e != null
                ? withStacktrace(line, e)
                : line;
        } catch (Exception ex) {
            return ouch(record, ex);
        }
    }

    private String line(LogRecord rec) {
        String formattedMessage = getFormattedMessage(rec);
        long id = rec.getLongThreadID();
        ZonedDateTime dateTime = rec.getInstant().truncatedTo(MILLIS).atZone(ZoneId.of("Z"));
        String threadName = threadNamer != null
            ? threadNamer.apply(id).orElseGet(() -> simple(id))
            : simple(id);
        return String.format(
            DEFAULT_FORMAT,
            dateTime,
            rec.getLevel().getName(),
            rec.getLoggerName(),
            formattedMessage,
            threadName
        );
    }

    private static final AtomicReference<DefaultLogFormatter> INSTANCE = new AtomicReference<>();

    private static final String DEFAULT_FORMAT = "%1$TFT%1$TH:%1$TM:%1$TS:%1$TLZ %2$-7s %3$s: %4$s [%5$s]%n";

    @SuppressWarnings("StaticCollection")
    private static final Map<String, MessageFormat> FORMATS = new ConcurrentHashMap<>();

    private static final Pattern NUMBERED_ARG = Pattern.compile("\\{\\d+}");

    private static final String EMPTY_ARG = "{}";

    private static final Pattern EMPTY_ARG_PATTERN = Pattern.compile("\\{}");

    private static String withStacktrace(String line, Throwable e) {
        String ln = line.endsWith("\n") ? "" : "\n";
        return line + ln + stackTrace(e);
    }

    private static String simple(long id) {
        return id == 1 ? "main" : String.valueOf(id);
    }

    private static String getFormattedMessage(LogRecord rec) {
        Object[] parameters = rec.getParameters();
        if (parameters == null || parameters.length == 0) {
            return rec.getMessage();
        }
        boolean rewrite = isEmptySetNotation(rec.getMessage());
        MessageFormat messageFormat = FORMATS.computeIfAbsent(
            rec.getMessage(),
            message ->
                createMessageFormat(message, parameters.length, rewrite)
        );
        return messageFormat.format(rewrite ? stringified(parameters) : parameters);
    }

    private static Object[] stringified(Object[] pars) {
        for (int i = 0; i < pars.length; i++) {
            Object par = pars[i];
            if (!(par instanceof CharSequence)) {
                pars[i] = par == null ? "null"
                    : par instanceof Number ? String.valueOf(par)
                        : pars[i];
            }
        }
        return pars;
    }

    private static MessageFormat createMessageFormat(String message, int length, boolean rewrite) {
        return new MessageFormat(
            rewrite
                ? indexed(message, length)
                : message,
            Locale.ROOT
        );
    }

    private static boolean isEmptySetNotation(String message) {
        return message.contains(EMPTY_ARG) && !NUMBERED_ARG.matcher(message).matches();
    }

    private static String indexed(CharSequence pattern, int paramsCount) {
        String[] parts = EMPTY_ARG_PATTERN.split(pattern);
        StringBuilder stringBuilder = new StringBuilder(pattern.length() + paramsCount);
        int param = 0;
        for (String part: parts) {
            stringBuilder.append(part);
            if (param < paramsCount) {
                stringBuilder.append("{").append(param).append(",}");
            }
            param++;
        }
        return stringBuilder.toString();
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private static String ouch(LogRecord record, Exception ex) {
        System.err.println("Logging failed: " + record);
        ex.printStackTrace(System.err);
        return ex.toString();
    }

    private static String stackTrace(Throwable e) {
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream(16384);
            PrintStream printStream = new PrintStream(baos, false, UTF_8)
        ) {
            e.printStackTrace(printStream);
            printStream.flush();
            return baos.toString(UTF_8);
        } catch (Exception ex) {
            return "Failed to retieve stacktrace: " + ex;
        }
    }
}
