package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.HashKind;

@SuppressWarnings("unused")
public final class Canonicalizers {

    public static <MK, K extends HashKind<K>> Canonicalizer<MK, K> canonicalizer() {
        return InternalFactory.canonicalizer();
    }

    public static <MK, K extends HashKind<K>> Canonicalizer<MK, K> canonicalizer(
        boolean collisionsNeverHappen
    ) {
        return InternalFactory.canonicalizer(collisionsNeverHappen);
    }

    private Canonicalizers() {
    }
}
