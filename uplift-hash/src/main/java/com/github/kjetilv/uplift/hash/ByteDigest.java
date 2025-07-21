package com.github.kjetilv.uplift.hash;

interface ByteDigest<K extends HashKind<K>> {

    K kind();

    void digest(Bytes bytes);

    Hash<K> get();
}
