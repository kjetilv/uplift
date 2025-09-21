package com.github.kjetilv.uplift.edamame;

import module uplift.hash;
import com.github.kjetilv.uplift.edamame.impl.InternalFactory;

@SuppressWarnings("unused")
public final class Canonicalizers {

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer() {
        return InternalFactory.canonicalizer();
    }

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer(
        boolean collisionsNeverHappen
    ) {
        return InternalFactory.canonicalizer(collisionsNeverHappen);
    }

    private Canonicalizers() {
    }
}
