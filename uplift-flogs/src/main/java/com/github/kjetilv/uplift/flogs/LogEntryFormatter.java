package com.github.kjetilv.uplift.flogs;

import java.time.ZonedDateTime;

final class LogEntryFormatter extends AbstractFormatter<LogEntry> {

    static final LogFormatter<LogEntry> INSTANCE = new LogEntryFormatter();

    private LogEntryFormatter() {
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    String loggableLine(LogEntry entry) {
        ZonedDateTime dateTime = entry.zuluTime();
        String formattedMessage = formatMessage(
            entry.msg(),
            entry.args(),
            entry.lastArgThrowable() ? 1 : 0
        );
        int year = dateTime.getYear();
        int mon = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        int hr = dateTime.getHour();
        int min = dateTime.getMinute();
        int sec = dateTime.getSecond();
        StringBuilder sb = new StringBuilder()
            .append(year)
            .append("-")
            .append(pad(mon)).append(mon)
            .append("-")
            .append(pad(day)).append(day)
            .append("T")
            .append(pad(hr)).append(hr)
            .append(':')
            .append(pad(min)).append(min)
            .append(':')
            .append(pad(sec)).append(sec);
        if (entry.hasLevel()) {
            sb.append(' ')
                .append(entry.logLevel().name());
        }
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

    private static String pad(int i) {
        return i < 10 ? "0" : "";
    }
}
