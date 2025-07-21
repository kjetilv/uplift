package com.github.kjetilv.uplift.hash;

import java.util.function.Supplier;

@FunctionalInterface
public interface Hashed<K extends HashKind<K>> extends Supplier<Hash<K>> {

    @Override
    default Hash<K> get() {
        return hash();
    }

    Hash<K> hash();
}
