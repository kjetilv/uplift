package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.HashKind;

public interface Canonicalizer<K, H extends HashKind<H>> {

    CanonicalValue<H> canonical(Object value);

    CanonicalValue<H> canonical(HashedTree<K, H> hashedTree);
}
