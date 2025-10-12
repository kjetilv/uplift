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
        var year = dateTime.getYear();
        var mon = dateTime.getMonthValue();
        var day = dateTime.getDayOfMonth();
        var hr = dateTime.getHour();
        var min = dateTime.getMinute();
        var sec = dateTime.getSecond();
        var sb = new StringBuilder()
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

    static final LogFormatter<LogEntry> INSTANCE = new LogEntryFormatter();

    private static String pad(int i) {
        return i < 10 ? "0" : "";
    }
}
