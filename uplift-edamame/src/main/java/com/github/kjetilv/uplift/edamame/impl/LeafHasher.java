package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.PojoBytes;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

/**
 * Strategy for hashing leaves.  {@link MapMemoizerFactory#create(KeyHandler, LeafHasher, HashKind, PojoBytes) Overridable}
 * for testing purposes.
 */
@FunctionalInterface
public interface LeafHasher<H extends HashKind<H>> {

    Hash<H> hash(Object leaf);
}
