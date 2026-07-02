package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.HashKind;

@SuppressWarnings("unused")
public final class Canonicalizers {

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer() {
        return canonicalizer(false);
    }

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer(
        boolean assumeNoCollisions
    ) {
        return InternalFactory.canonicalizer(assumeNoCollisions);
    }

    private Canonicalizers() {
    }
}
