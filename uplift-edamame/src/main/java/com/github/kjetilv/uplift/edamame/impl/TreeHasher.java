package com.github.kjetilv.uplift.edamame.impl;

import module uplift.hash;
import com.github.kjetilv.uplift.edamame.HashedTree;

@FunctionalInterface
interface TreeHasher<K, H extends HashKind<H>> {

    HashedTree<K, H> hash(Object value);
}
