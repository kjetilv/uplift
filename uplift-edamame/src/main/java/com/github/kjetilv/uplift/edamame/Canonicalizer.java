package com.github.kjetilv.uplift.edamame;

import module uplift.hash;

@FunctionalInterface
public interface Canonicalizer<K, H extends HashKind<H>> {

    CanonicalValue<H> canonical(HashedTree<K, H> hashedTree);
}
