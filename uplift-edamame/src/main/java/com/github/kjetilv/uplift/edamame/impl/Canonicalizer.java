package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.hash.HashKind;

interface Canonicalizer<K, H extends HashKind<H>> {

    CanonicalValue<H> canonical(Object value);

    CanonicalValue<H> canonical(HashedTree<K, H> hashedTree);
}
