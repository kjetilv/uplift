package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import module uplift.hash;

import com.github.kjetilv.uplift.edam.Info;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;

/// Storage for [Throwable]s.
@SuppressWarnings("unused")
interface InfoProvider<T, I extends Info<T, K>, K extends HashKind<K>> {

    default I build(T item, Occurrence<K> occurrence) {
        return build(item, occurrence, null);
    }

    I build(T item, Occurrence<K> occurrence, Function<Throwable, String> printer);
}
