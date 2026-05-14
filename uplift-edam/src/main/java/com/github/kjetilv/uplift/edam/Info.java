package com.github.kjetilv.uplift.edam;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

public interface Info<T, H extends HashKind<H>> {

    T source();

    Hash<H> hash();
}
