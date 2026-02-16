package com.github.kjetilv.uplift.flogs;

import module java.base;

final class BriefLogEntryFormatter extends AbstractLogEntryFormatter {

    @Override
    protected String name(LogEntry entry) {
        return entry.shortName();
    }
}
