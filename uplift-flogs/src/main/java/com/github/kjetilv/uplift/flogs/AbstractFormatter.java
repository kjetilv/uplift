package com.github.kjetilv.uplift.flogs;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

abstract class AbstractFormatter<E> implements LogFormatter<E> {

    /// Substitutes parameters into a message, as SLF4J does:
    ///
    /// * Each `{}` is replaced by the next parameter, in order
    /// * `\{}` is a literal `{}`, and `\\{}` is a backslash followed by a parameter
    /// * A `{}` without a parameter is kept as it is, and parameters without a `{}` are ignored
    /// * Arrays are written as their contents
    /// * A parameter whose `toString` fails is written as `[FAILED toString()]`
    ///
    /// No other characters are special. The parameters are not modified.
    ///
    /// @param baseMessage Message, may be null
    /// @param parameters  Parameters, may be null
    /// @param trim        Number of trailing parameters to leave out, such as a throwable
    /// @return The message with parameters substituted
    static String formatMessage(String baseMessage, Object[] parameters, int trim) {
        var message = String.valueOf(baseMessage);
        var count = parameters == null ? 0 : parameters.length - trim;
        if (count <= 0) {
            return message;
        }
        var first = message.indexOf(PLACEHOLDER);
        if (first < 0) {
            return message;
        }
        var sb = new StringBuilder(message.length() + 16 * count);
        var start = 0;
        var param = 0;
        for (var index = first; index >= 0 && param < count; index = message.indexOf(PLACEHOLDER, start)) {
            var escapes = backslashesBefore(message, index, start);
            if (escapes == 1) {
                sb.append(message, start, index - 1).append(PLACEHOLDER);
            } else {
                sb.append(message, start, escapes == 2 ? index - 1 : index);
                append(sb, parameters[param++], null);
            }
            start = index + PLACEHOLDER.length();
        }
        return sb.append(message, start, message.length()).toString();
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

    private static final String PLACEHOLDER = "{}";

    /// @return 1 if the placeholder is escaped by one backslash, 2 if preceded by an escaped backslash, else 0
    private static int backslashesBefore(String message, int index, int start) {
        if (index - 1 < start || message.charAt(index - 1) != '\\') {
            return 0;
        }
        return index - 2 >= start && message.charAt(index - 2) == '\\' ? 2 : 1;
    }

    /// Appends a parameter. Arrays are written as their contents; an array that contains itself is written
    /// as `[...]` where it recurs.
    ///
    /// @param seen Arrays being written, created when the first object array is met
    private static void append(StringBuilder sb, Object parameter, Map<Object, Boolean> seen) {
        switch (parameter) {
            case null -> sb.append("null");
            case Object[] array -> {
                var arrays = seen == null ? new IdentityHashMap<Object, Boolean>() : seen;
                if (arrays.put(array, Boolean.TRUE) != null) {
                    sb.append("[...]");
                    return;
                }
                sb.append('[');
                for (var i = 0; i < array.length; i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    append(sb, array[i], arrays);
                }
                sb.append(']');
                arrays.remove(array);
            }
            case boolean[] array -> sb.append(Arrays.toString(array));
            case byte[] array -> sb.append(Arrays.toString(array));
            case char[] array -> sb.append(Arrays.toString(array));
            case short[] array -> sb.append(Arrays.toString(array));
            case int[] array -> sb.append(Arrays.toString(array));
            case long[] array -> sb.append(Arrays.toString(array));
            case float[] array -> sb.append(Arrays.toString(array));
            case double[] array -> sb.append(Arrays.toString(array));
            default -> {
                try {
                    sb.append(parameter);
                } catch (Exception e) {
                    sb.append("[FAILED toString()]");
                }
            }
        }
    }

    private static String withStacktrace(String line, Throwable e) {
        var ln = line.endsWith("\n") ? "" : "\n";
        return line + ln + stackTrace(e);
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
