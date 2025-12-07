package com.github.kjetilv.uplift.flogs;

import module java.base;

final class FLoggers {

    private final Consumer<String> printer;

    private final Consumer<String> emergencyPrinter;

    private final Flogs.Settings settings;

    FLoggers(Consumer<String> printer, Flogs.Settings settings) {
        this.printer = Objects.requireNonNull(printer, "printer");
        this.settings = settings;
        this.emergencyPrinter = System.err::println;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings.logLevel() + "]";
    }

    FLogger create(String name) {
        return new FLogger(
            name,
            printer,
            emergencyPrinter,
            settings
        );
    }
}
