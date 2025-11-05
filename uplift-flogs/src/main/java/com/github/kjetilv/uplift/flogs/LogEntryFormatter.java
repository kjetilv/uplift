package com.github.kjetilv.uplift.flogs;

import module java.base;

final class LogEntryFormatter extends AbstractFormatter<LogEntry> {

    private LogEntryFormatter() {
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    String loggableLine(LogEntry entry) {
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
            .append('-').append(padding(mon)).append(mon)
            .append('-').append(padding(day)).append(day)
            .append('T').append(padding(hr)).append(hr)
            .append(':').append(padding(min)).append(min)
            .append(':').append(padding(sec)).append(sec);
        String logLevel = entry.logLevel().name();
        sb.append(' ')
            .append(SPACES, 0, 5 - logLevel.length())
            .append(logLevel);
        sb.append(' ')
            .append(entry.name())
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
    Throwable throwable(LogEntry entry) {
        return entry.throwable();
    }

    static final LogFormatter<LogEntry> INSTANCE = new LogEntryFormatter();

    private static final String SPACES = IntStream.range(0, 64)
        .mapToObj(_ -> " ")
        .collect(Collectors.joining());

    private static String padding(int i) {
        return i < 10 ? "0" : "";
    }
}
