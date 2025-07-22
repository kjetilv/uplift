package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.MapMemoizerFactory;
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
