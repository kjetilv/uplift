package com.github.kjetilv.uplift.hash;

import module uplift.util;

interface ByteDigest<H extends HashKind<H>> {

    /**
     * @return The kind of hash
     */
    H kind();

    /**
     * Add bytes to digest.
     *
     * @param bytes Bytes
     */
    void digest(Bytes bytes);

    /**
     * @return The hash of the bytes added to the digest.
     */
    Hash<H> get();
}
