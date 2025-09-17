package com.github.kjetilv.uplift.edam;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

public interface Info<T, K extends HashKind<K>> {

    T source();

    Hash<K> hash();
}
