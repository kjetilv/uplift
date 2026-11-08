package com.github.kjetilv.uplift.flogs.test;

import module java.base;
import com.github.kjetilv.uplift.flogs.LogLevel;
import com.github.kjetilv.uplift.flogs.Logger;

/// A flogs logger that records its calls, enabled at and above a threshold
final class Capture implements Logger {

    final List<Call> calls = new CopyOnWriteArrayList<>();

    private final LogLevel threshold;

    Capture(LogLevel threshold) {
        this.threshold = threshold;
    }

    @Override
    public String name(boolean shorten) {
        return "test";
    }

    @Override
    public boolean isEnabled(LogLevel logLevel) {
        return logLevel.ordinal() <= threshold.ordinal();
    }

    @Override
    public void log(LogLevel logLevel, String format, Object... arguments) {
        calls.add(new Call(logLevel, format, Arrays.asList(arguments)));
    }

    record Call(LogLevel level, String message, List<Object> args) {
    }
}
