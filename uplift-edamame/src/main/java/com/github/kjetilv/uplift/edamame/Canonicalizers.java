package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.InternalFactory;
import com.github.kjetilv.uplift.hash.HashKind;

@SuppressWarnings("unused")
public final class Canonicalizers {

    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer() {
        return canonicalizer(false);
    }

    /// @param assumeNoCollisions If true, a value whose hash is already known is taken to be equal to the
    ///                           known value, without checking. This is faster, but if two different values
    ///                           do share a hash, the second is silently replaced by the first.
    ///
    ///                           Only use this with [K256][com.github.kjetilv.uplift.hash.HashKind.K256]
    ///                           hashes, or with trusted input. [K128][com.github.kjetilv.uplift.hash.HashKind.K128]
    ///                           uses MD5, for which colliding inputs can be constructed on purpose.
    /// @return Canonicalizer
    public static <K, H extends HashKind<H>> Canonicalizer<K, H> canonicalizer(
        boolean assumeNoCollisions
    ) {
        return InternalFactory.canonicalizer(assumeNoCollisions);
    }

    private Canonicalizers() {
    }
}
