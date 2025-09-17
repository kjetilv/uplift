package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Info;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.function.Function;

/**
 * Storage for {@link Throwable}s.
 */
@SuppressWarnings("unused")
interface InfoProvider<T, I extends Info<T, K>, K extends HashKind<K>> {

    default I build(T item, Occurrence<K> occurrence) {
        return build(item, occurrence, null);
    }

    I build(T item, Occurrence<K> occurrence, Function<Throwable, String> printer);
}
