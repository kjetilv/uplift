package com.github.kjetilv.uplift.flogs;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractLogEntryFormatter extends AbstractFormatter<LogEntry> {

    @SuppressWarnings("DuplicatedCode")
    @Override
    final String loggableLine(LogEntry entry) {
        var sb = new StringBuilder();
        var dateTime = entry.zuluTime();
        var formattedMessage = formatMessage(
            entry.msg(),
            entry.args(),
            entry.lastArgThrowable() ? 1 : 0
        );
        var dateFormatted =
            dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        var level = entry.logLevel();
        sb.append(dateFormatted)
            .append(SPACES, 0, ISO_LENGTH - dateFormatted.length())
            .append(' ')
            .append(SPACES, 0, 5 - level.length());
        if (AWS_LAMBDA) {
            sb.append(color(level))
                .append(level.name())
                .append(COLOR_OFF)
                .append(' ')
                .append(BOLD_ON)
                .append(name(entry))
                .append(BOLD_OFF)
                .append(": ")
                .append(formattedMessage)
                .append(" ")
                .append(BOLD_ON)
                .append(THREAD_ICON)
                .append(BOLD_OFF)
                .append(ITAL_ON)
                .append(entry.threadName())
                .append(ITAL_OFF);
        } else {
            sb.append(level.name())
                .append(' ')
                .append(name(entry))
                .append(" ")
                .append(formattedMessage)
                .append(" ")
                .append(THREAD_ICON)
                .append(entry.threadName());
        }
        return sb.toString();
    }

    @Override
    final Throwable throwable(LogEntry entry) {
        return entry.throwable();
    }

    protected abstract String name(LogEntry entry);

    private static final int ISO_LENGTH = 24;

    private static final char[] EMPTY = new char[0];

    private static final char ESC = 0x1b;

    private static final char PAR = '[';

    private static final char END = 'm';

    private static final boolean AWS_LAMBDA = System.getProperty("_X_AMZN_TRACE_ID") != null;

    private static final char[] RED = ifAws(ESC, PAR, '3', '1', END);

    private static final char[] YELLOW = ifAws(ESC, PAR, '3', '3', END);

    private static final char[] GREEN = ifAws(ESC, PAR, '3', '2', END);

    private static final char[] CYAN = ifAws(ESC, PAR, '3', '6', END);

    private static final char[] BOLD_ON = ifAws(ESC, PAR, '1', END);

    private static final char[] BOLD_OFF = ifAws(ESC, PAR, '2', '2', END);

    private static final char[] ITAL_ON = ifAws(ESC, PAR, '3', END);

    private static final char[] ITAL_OFF = ifAws(ESC, PAR, '2', '3', END);

    private static final char[] COLOR_OFF = ifAws(ESC, PAR, '0', END);

    private static final char[] SPACES = IntStream.range(0, 64)
        .mapToObj(_ -> " ")
        .collect(Collectors.joining())
        .toCharArray();

    public static final String THREAD_ICON = "🧵";

    private static char[] color(LogLevel level) {
        return switch (level) {
            case ERROR -> RED;
            case WARN -> YELLOW;
            case INFO -> GREEN;
            case DEBUG -> CYAN;
            default -> EMPTY;
        };
    }

    private static char[] ifAws(char... chars) {
        return AWS_LAMBDA ? EMPTY : chars;
    }
}
