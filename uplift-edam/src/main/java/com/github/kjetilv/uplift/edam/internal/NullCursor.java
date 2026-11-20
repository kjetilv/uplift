package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

final class NullCursor<T extends Supplier<Hash<H>>, H extends HashKind<H>> implements Storage.Cursor<T, H> {

    public static <T extends Supplier<Hash<H>>, H extends HashKind<H>> Storage.Cursor<T, H> create() {
        return new NullCursor<>();
    }

    @Override
    public Stream<T> spool(Instant limit) {
        return Stream.empty();
    }

    @Override
    public Optional<T> next() {
        return Optional.empty();
    }
}
