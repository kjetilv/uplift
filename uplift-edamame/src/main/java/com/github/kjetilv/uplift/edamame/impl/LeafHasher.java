package com.github.kjetilv.uplift.edamame.impl;

import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.edamame.PojoBytes;

/**
 * Strategy for hashing leaves.  {@link MapMemoizerFactory#create(KeyHandler, PojoBytes, LeafHasher) Overridable}
 * for testing purposes.
 */
public interface LeafHasher {

    Hash hash(Object leaf);
}
