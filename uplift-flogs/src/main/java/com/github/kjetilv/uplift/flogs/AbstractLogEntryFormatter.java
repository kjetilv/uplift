package com.github.kjetilv.uplift.flogs;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractLogEntryFormatter extends AbstractFormatter<LogEntry> {

    @SuppressWarnings("DuplicatedCode")
    @Override
    final String loggableLine(LogEntry entry) {
        var dateTime = entry.zuluTime();
        var formattedMessage = formatMessage(
            entry.msg(),
            entry.args(),
            entry.lastArgThrowable() ? 1 : 0
        );
        var mon = dateTime.getMonthValue();
        var day = dateTime.getDayOfMonth();
        var hr = dateTime.getHour();
        var min = dateTime.getMinute();
        var sec = dateTime.getSecond();
        var sb = new StringBuilder()
            .append(dateTime.getYear())
            .append('-').append(pad(mon)).append(mon)
            .append('-').append(pad(day)).append(day)
            .append('T').append(pad(hr)).append(hr)
            .append(':').append(pad(min)).append(min)
            .append(':').append(pad(sec)).append(sec);
        var logLevel = entry.logLevel().name();
        sb.append(' ')
            .append(SPACES, 0, 5 - logLevel.length())
            .append(logLevel)
            .append(' ')
            .append(name(entry))
            .append(": ")
            .append(formattedMessage);
        if (entry.threadName() != null) {
            sb.append(" [")
                .append(entry.threadName())
                .append(']');
        }
        return sb.toString();
    }

    @Override
    final Throwable throwable(LogEntry entry) {
        return entry.throwable();
    }

    protected abstract String name(LogEntry entry);

    private static final String SPACES = IntStream.range(0, 64)
        .mapToObj(_ -> " ")
        .collect(Collectors.joining());

    private static String pad(int i) {
        return i < 10 ? "0" : "";
    }
}
