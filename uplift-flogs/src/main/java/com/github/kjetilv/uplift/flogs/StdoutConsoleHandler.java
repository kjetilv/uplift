package com.github.kjetilv.uplift.flogs;

import java.nio.charset.StandardCharsets;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;

import static java.util.logging.Level.FINE;

final class StdoutConsoleHandler extends ConsoleHandler {

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    StdoutConsoleHandler(Formatter formatter) {
        try {
            setEncoding(UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to setup encoding " + UTF_8, e);
        }
        setOutputStream(System.out);
        setLevel(FINE);
        setFormatter(formatter);
    }
    private static final String UTF_8 = StandardCharsets.UTF_8.name();
}
