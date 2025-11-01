package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.HashKind;

@FunctionalInterface
public interface Canonicalizer<K, H extends HashKind<H>> {

    CanonicalValue<H> canonical(HashedTree<K, H> hashedTree);
}
