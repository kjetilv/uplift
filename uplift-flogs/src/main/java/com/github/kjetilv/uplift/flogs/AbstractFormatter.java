package com.github.kjetilv.uplift.flogs;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

abstract class AbstractFormatter<E> implements LogFormatter<E> {

    static String formatMessage(String baseMessage, Object[] parameters, int trim) {
        if (parameters == null || parameters.length - trim <= 0) {
            return baseMessage;
        }
        var rewrite = baseMessage.contains(EMPTY_ARG);
        var messageFormat = FORMATS.computeIfAbsent(
            trim > 0
                ? baseMessage + "-" + trim
                : baseMessage,
            _ -> {
                var pattern = rewrite
                    ? indexed(baseMessage, parameters.length - trim)
                    : baseMessage;
                return new MessageFormat(pattern, Locale.ROOT);
            }
        );
        return messageFormat.format(rewrite ? stringified(parameters) : parameters);
    }

    public String format(E entry) {
        var line = loggableLine(entry);
        var e = throwable(entry);
        return e != null
            ? withStacktrace(line, e)
            : line;
    }

    abstract String loggableLine(E entry);

    abstract Throwable throwable(E entry);

    @SuppressWarnings("StaticCollection")
    private static final Map<String, MessageFormat> FORMATS = new ConcurrentHashMap<>();

    private static final String EMPTY_ARG = "{}";

    private static final Pattern EMPTY_ARG_PATTERN = Pattern.compile("\\{}");

    private static String withStacktrace(String line, Throwable e) {
        var ln = line.endsWith("\n") ? "" : "\n";
        return line + ln + stackTrace(e);
    }

    private static Object[] stringified(Object[] pars) {
        for (var i = 0; i < pars.length; i++) {
            pars[i] = String.valueOf(pars[i]);
        }
        return pars;
    }

    private static String indexed(CharSequence pattern, int paramsCount) {
        var parts = EMPTY_ARG_PATTERN.split(pattern);
        var stringBuilder = new StringBuilder(pattern.length() + paramsCount);
        var param = 0;
        for (var part : parts) {
            stringBuilder.append(part);
            if (param < paramsCount) {
                stringBuilder.append("{").append(param).append(",}");
            }
            param++;
        }
        return stringBuilder.toString();
    }

    private static String stackTrace(Throwable e) {
        try (
            var baos = new ByteArrayOutputStream(16384);
            var printStream = new PrintStream(baos, false, UTF_8)
        ) {
            e.printStackTrace(printStream);
            printStream.flush();
            return baos.toString(UTF_8);
        } catch (Exception ex) {
            return "Failed to retrieve stacktrace: " + ex;
        }
    }
}
