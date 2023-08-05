package com.github.kjetilv.uplift.flogs;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;

class DefaultLogFormatterTest {

    @Test
    void format() {
        DefaultLogFormatter formatter = new DefaultLogFormatter();
        LogRecord record = new LogRecord(
            Level.INFO,
            "Foo {} bar {}"
        );
        record.setParameters(
            new Object[] { 1, 2, 3 }
        );
        record.setThrown(new Throwable());
        String format = formatter.format(record);
        System.out.println(format);
    }
}
