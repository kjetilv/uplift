package com.github.kjetilv.uplift.flogs;

import module java.base;

final class Floggers {

    private final Consumer<String> printer;

    private final Consumer<String> emergencyPrinter;

    private final Flogs.Settings settings;

    Floggers(Consumer<String> printer, Flogs.Settings settings) {
        this.printer = Objects.requireNonNull(printer, "printer");
        this.settings = settings;
        this.emergencyPrinter = System.err::println;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + settings.logLevel() + "]";
    }

    Flogger create(String name) {
        return new Flogger(name, printer, emergencyPrinter, settings);
    }
}
