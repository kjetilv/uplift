package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.HashKind;

public final class Canonicalizers {

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer(H kind) {
        return canonicalizer(kind, null);
    }

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer(
        H kind,
        KeyHandler<K> keyHandler
    ) {
        return canonicalizer(kind, keyHandler, null, null);
    }

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer(
        H kind,
        KeyHandler<K> keyHandler,
        LeafHasher<H> leafHasher,
        PojoBytes pojoBytes
    ) {
        return InternalFactory.canonicalValues(kind, keyHandler, leafHasher, pojoBytes);
    }

    private Canonicalizers() {
    }
}
