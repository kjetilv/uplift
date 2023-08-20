package com.github.kjetilv.uplift.flogs;

import java.time.ZonedDateTime;

final class LogEntryFormatter extends AbstractFormatter<LogEntry> {

    @Override
    String loggableLine(LogEntry entry) {
        ZonedDateTime dateTime = entry.zuluTime();
        String threadName = entry.threadName();
        LogLevel level = entry.logLevel();
        String formattedMessage = formatMessage(
            entry.msg(),
            entry.args(),
            entry.lastArgThrowable() ? 1 : 0
        );
        if (entry.isInfo()) {
            return String.format(
                DEFAULT_LEVEL_FORMAT,
                dateTime,
                entry.name(),
                formattedMessage,
                threadName
            );
        }
        return String.format(
            DEFAULT_FORMAT,
            dateTime,
            level.name(),
            entry.name(),
            formattedMessage,
            threadName
        );
    }

    @Override
    Throwable throwable(LogEntry entry) {
        return entry.throwable();
    }
}
