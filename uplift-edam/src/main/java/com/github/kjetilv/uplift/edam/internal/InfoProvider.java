package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.Info;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.HashKind;

/// Storage for [Throwable]s.
@SuppressWarnings("unused")
interface InfoProvider<T, I extends Info<T, H>, H extends HashKind<H>> {

    default I build(T item, Occurrence<H> occurrence) {
        return build(item, occurrence, null);
    }

    I build(T item, Occurrence<H> occurrence, Function<Throwable, String> printer);
}
