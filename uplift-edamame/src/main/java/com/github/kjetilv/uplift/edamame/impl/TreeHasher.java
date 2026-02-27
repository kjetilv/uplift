package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.hash.HashKind;

@FunctionalInterface
interface TreeHasher<MK, K extends HashKind<K>> {

    HashedTree<MK, K> tree(Object value);
}
