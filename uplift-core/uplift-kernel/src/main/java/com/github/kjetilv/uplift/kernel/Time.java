package com.github.kjetilv.uplift.kernel;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Supplier;

public final class Time {

    public static final Clock UTC_CLOCK = Clock.system(ZoneId.of("UTC"));

    public static Supplier<Instant> utcSupplier() {
        return UTC_CLOCK::instant;
    }

    private Time() {

    }
}
