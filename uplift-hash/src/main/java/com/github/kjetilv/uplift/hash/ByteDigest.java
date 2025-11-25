package com.github.kjetilv.uplift.hash;

import com.github.kjetilv.uplift.util.Bytes;

/// Hides the details of byte digestion
interface ByteDigest<H extends HashKind<H>> {

    /// @return The kind of hash
    H kind();

    /// Add bytes to digest.
    ///
    /// @param bytes Bytes
    void digest(Bytes bytes);

    /// Resets the current digest and returns th bytes digested so far
    ///
    ///  @return The hash of the bytes added to the digest
    Hash<H> get();
}
