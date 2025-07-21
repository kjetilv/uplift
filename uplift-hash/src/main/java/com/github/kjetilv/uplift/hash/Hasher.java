package com.github.kjetilv.uplift.hash;

import java.util.function.Function;

/**
 * Strategy interface for identifying objects. Thread-safe.
 */
@FunctionalInterface
public interface Hasher<T, K extends HashKind<K>> extends Function<T, Hash<K>> {

    @Override
    default Hash<K> apply(T t) {
        return hash(t);
    }

    /**
     * @param t Non-null
     * @return Id
     */
    Hash<K> hash(T t);
}
