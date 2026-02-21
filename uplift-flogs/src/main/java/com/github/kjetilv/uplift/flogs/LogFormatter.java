package com.github.kjetilv.uplift.flogs;

import module java.base;

public interface LogFormatter<E> {

    LogFormatter<LogEntry> DEFAULT = new DefaultLogEntryFormatter();

    LogFormatter<LogEntry> BRIEF = new BriefLogEntryFormatter();

    String format(E entry);
}
