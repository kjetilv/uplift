package com.github.kjetilv.uplift.edam;

import module uplift.hash;

public interface Info<T, K extends HashKind<K>> {

    T source();

    Hash<K> hash();
}
