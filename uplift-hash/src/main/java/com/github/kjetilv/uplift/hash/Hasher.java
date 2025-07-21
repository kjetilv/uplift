package com.github.kjetilv.uplift.hash;

import java.util.function.Function;

/**
 * Strategy interface for identifying objects. Thread-safe.
 */
@FunctionalInterface
public interface Hasher<T, H extends HashKind<H>> extends Function<T, Hash<H>> {

    @Override
    default Hash<H> apply(T t) {
        return hash(t);
    }

    /**
     * @param t Non-null
     * @return Id
     */
    Hash<H> hash(T t);
}
