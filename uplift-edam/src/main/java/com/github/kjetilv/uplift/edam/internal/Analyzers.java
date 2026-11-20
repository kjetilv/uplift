package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.HashKind;

public final class Analyzers {

    public static <H extends HashKind<H>> Analyzer<Throwable, H> analyzer(
        Hasher<Throwable, H> hasher,
        Storage<Occurrence<H>, H> storage,
        InstantSource now,
        int maxLength
    ) {
        return new AnalyzerImpl<>(hasher, storage, now, maxLength);
    }

    private Analyzers() {

    }
}
