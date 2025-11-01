package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.HashKind;

final class NullCursor<K extends HashKind<K>> implements Storage.Cursor<K> {

    public static <K extends HashKind<K>> Storage.Cursor<K> create() {
        return new NullCursor<>();
    }

    @Override
    public Stream<Occurrence<K>> spool(Instant limit) {
        return Stream.empty();
    }

    @Override
    public Optional<Occurrence<K>> next() {
        return Optional.empty();
    }
}
