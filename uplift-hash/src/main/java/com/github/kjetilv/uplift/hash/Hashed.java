package com.github.kjetilv.uplift.hash;

import java.util.function.Supplier;

@FunctionalInterface
public interface Hashed<H extends HashKind<H>> extends Supplier<Hash<H>> {

    @Override
    default Hash<H> get() {
        return hash();
    }

    Hash<H> hash();
}
