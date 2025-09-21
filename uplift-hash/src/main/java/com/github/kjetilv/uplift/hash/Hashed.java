package com.github.kjetilv.uplift.hash;

import module java.base;

@FunctionalInterface
public interface Hashed<H extends HashKind<H>> extends Supplier<Hash<H>> {

    @Override
    default Hash<H> get() {
        return hash();
    }

    Hash<H> hash();
}
