package com.github.kjetilv.uplift.synchttp.read;

import module java.base;

/// The body of one request. Reading never goes past the end of the body on the connection, except for
/// [ChunkedBody], which hands back bytes it read past the end, see [#leftover()].
sealed interface RequestBody extends ReadableByteChannel permits BodyBytes, ChunkedBody {

    /// @return True if the whole body has been read from the connection, or skipped in the request buffer
    boolean complete();

    /// Reads and discards the rest of the body, if it can be done within `limit` bytes.
    ///
    /// @param limit Max number of bytes to discard
    /// @return True if the body is now complete, see [#complete()]
    boolean drain(long limit);

    /// @return True if the client waits for 100 Continue before it sends the rest of the body
    boolean awaitingContinue();

    /// @return Number of bytes of the request buffer that belong to this body
    int bufferedLength();

    /// @return Bytes read from the connection past the end of the body. They are the start of the next request.
    ByteBuffer leftover();

    /// Does nothing. The connection belongs to the server.
    @Override
    default void close() {
    }
}
