package com.github.kjetilv.uplift.hash;

interface ByteDigest<H extends HashKind<H>> {

    H kind();

    void digest(Bytes bytes);

    Hash<H> get();
}
