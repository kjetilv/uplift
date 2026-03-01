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
        var dateFormatted = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        sb.append(dateFormatted);
        sb.append(SPACES, 0, ISO_LENGTH - dateFormatted.length());
        var level = entry.logLevel();
        sb.append(' ')
            .append(SPACES, 0, 5 - level.length())
            .append(color(level))
            .append(level.name())
            .append(COLOR_OFF)
            .append(' ')
            .append(BOLD_ON)
            .append(name(entry))
            .append(BOLD_OFF)
            .append(AWS_LAMBDA ? ": " : " ")
            .append(formattedMessage);
        if (entry.threadName() != null) {
            sb.append(" ")
                .append(ITAL_ON)
                .append("[")
                .append(entry.threadName())
                .append(']')
                .append(ITAL_OFF);
        }
        return sb.toString();
    }

    @Override
    final Throwable throwable(LogEntry entry) {
        return entry.throwable();
    }

    protected abstract String name(LogEntry entry);

    private static final boolean AWS_LAMBDA = System.getProperty("_X_AMZN_TRACE_ID") != null;

    private static final char[] EMPTY = new char[0];

    private static final int ESC = 0x1b;

    private static final char PAR = '[';

    private static final char END = 'm';

    private static final char[] RED = AWS_LAMBDA ? EMPTY : new char[] {ESC, PAR, '3', '1', END};

    private static final char[] YELLOW = AWS_LAMBDA ? EMPTY : new char[] {ESC, PAR, '3', '3', END};

    private static final char[] GREEN = AWS_LAMBDA ? EMPTY : new char[] {ESC, PAR, '3', '2', END};

    private static final char[] BOLD_ON = AWS_LAMBDA ? EMPTY : new char[] {ESC, PAR, '1', END};

    private static final char[] BOLD_OFF = AWS_LAMBDA ? EMPTY : new char[] {ESC, PAR, '2', '2', END};

    private static final char[] ITAL_ON = AWS_LAMBDA ? EMPTY : new char[] {ESC, PAR, '3', END};

    private static final char[] ITAL_OFF = AWS_LAMBDA ? EMPTY : new char[] {ESC, PAR, '2', '3', END};

    private static final char[] COLOR_OFF = AWS_LAMBDA ? EMPTY : new char[] {ESC, PAR, '0', END};

    private static final String SPACES = IntStream.range(0, 64)
        .mapToObj(_ -> " ")
        .collect(Collectors.joining());

    public static final int ISO_LENGTH = 24;

    private static char[] color(LogLevel level) {
        return AWS_LAMBDA ? EMPTY
            : switch (level) {
                case ERROR -> RED;
                case WARN -> YELLOW;
                case INFO -> GREEN;
                default -> EMPTY;
            };
    }

    private static String pad(int i) {
        return i < 10 ? "0" : "";
    }
}
