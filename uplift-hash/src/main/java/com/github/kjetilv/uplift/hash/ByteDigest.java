package com.github.kjetilv.uplift.hash;

import com.github.kjetilv.uplift.kernel.io.Bytes;

interface ByteDigest<H extends HashKind<H>> {

    H kind();

    void digest(Bytes bytes);

    Hash<H> get();
}
