package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.hash.HashKind;

@FunctionalInterface
interface MapHasher<H extends HashKind<H>> {

    HashedTree<H> hashedTree(Object value);
}
