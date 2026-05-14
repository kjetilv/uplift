package com.github.kjetilv.uplift.edam.internal;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

/// Strategy interface for identifying objects. Thread-safe.
@FunctionalInterface
public interface Hasher<T, H extends HashKind<H>> extends Function<T, Hash<H>> {

    @Override
    default Hash<H> apply(T t) {
        return hash(t);
    }

    /// @param t Non-null
    /// @return Id
    Hash<H> hash(T t);
}
