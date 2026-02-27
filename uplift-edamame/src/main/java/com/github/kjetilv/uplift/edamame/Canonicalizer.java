package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.HashKind;

@FunctionalInterface
public interface Canonicalizer<MK, K extends HashKind<K>> {

    CanonicalValue<K> canonical(HashedTree<MK, K> hashedTree);
}
