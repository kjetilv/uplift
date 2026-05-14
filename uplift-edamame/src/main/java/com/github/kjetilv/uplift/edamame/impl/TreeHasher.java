package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.hash.HashKind;

@FunctionalInterface
interface TreeHasher<K, H extends HashKind<H>> {

    HashedTree<K, H> tree(Object value);
}
