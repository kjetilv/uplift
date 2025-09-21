package com.github.kjetilv.uplift.util;

import module java.base;

public final class Time {

    public static final Clock UTC_CLOCK = Clock.system(ZoneId.of("UTC"));

    public static Supplier<Instant> utcSupplier() {
        return UTC_CLOCK::instant;
    }

    private Time() {

    }
}
