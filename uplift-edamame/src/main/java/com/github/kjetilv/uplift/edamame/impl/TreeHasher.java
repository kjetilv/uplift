package com.github.kjetilv.uplift.edamame.impl;

import module uplift.edamame;
import module uplift.hash;

@FunctionalInterface
interface TreeHasher<K, H extends HashKind<H>> {

    HashedTree<K, H> hash(Object value);
}
