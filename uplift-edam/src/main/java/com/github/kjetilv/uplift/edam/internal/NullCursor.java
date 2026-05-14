package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.HashKind;

final class NullCursor<H extends HashKind<H>> implements Storage.Cursor<H> {

    public static <K extends HashKind<K>> Storage.Cursor<K> create() {
        return new NullCursor<>();
    }

    @Override
    public Stream<Occurrence<H>> spool(Instant limit) {
        return Stream.empty();
    }

    @Override
    public Optional<Occurrence<H>> next() {
        return Optional.empty();
    }
}
