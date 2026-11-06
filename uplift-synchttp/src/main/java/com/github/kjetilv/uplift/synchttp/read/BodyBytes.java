package com.github.kjetilv.uplift.synchttp.read;

import module java.base;

/// The body of one request, limited to its content length.
///
/// The first bytes may already be in the request buffer. The rest is read from the connection, and
/// never past the end of the body, so the next request on the connection is left in place.
@SuppressWarnings("StatementWithEmptyBody")
final class BodyBytes implements ReadableByteChannel {

    static final BodyBytes EMPTY = new BodyBytes(null, ByteBuffer.allocate(0), 0L);

    private final ReadableByteChannel channel;

    private final ByteBuffer buffered;

    private long unreadFromChannel;

    BodyBytes(ReadableByteChannel channel, ByteBuffer buffered, long unreadFromChannel) {
        this.channel = channel;
        this.buffered = Objects.requireNonNull(buffered, "buffered");
        this.unreadFromChannel = unreadFromChannel;
        if (unreadFromChannel > 0) {
            Objects.requireNonNull(channel, "channel");
        }
    }

    @Override
    public int read(ByteBuffer dst) {
        if (buffered.hasRemaining()) {
            var count = Math.min(dst.remaining(), buffered.remaining());
            dst.put(buffered.slice(buffered.position(), count));
            buffered.position(buffered.position() + count);
            return count;
        }
        if (unreadFromChannel == 0) {
            return -1;
        }
        var limit = dst.limit();
        if (dst.remaining() > unreadFromChannel) {
            dst.limit(dst.position() + Math.toIntExact(unreadFromChannel));
        }
        try {
            var read = channel.read(dst);
            if (read > 0) {
                unreadFromChannel -= read;
            }
            return read;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read from " + channel, e);
        } finally {
            dst.limit(limit);
        }
    }

    /// Reads and discards the rest of the body, until it is all read or the connection ends.
    void drain() {
        var discard = ByteBuffer.allocate(Math.toIntExact(Math.min(DRAIN_BUFFER_SIZE, unreadFromChannel)));
        buffered.position(buffered.limit());
        while (unreadFromChannel > 0 && read(discard.clear()) >= 0) {
        }
    }

    /// @return Number of body bytes not yet read from the connection
    long unreadFromChannel() {
        return unreadFromChannel;
    }

    @Override
    public boolean isOpen() {
        return buffered.hasRemaining() || unreadFromChannel > 0;
    }

    /// Does nothing. The connection belongs to the server.
    @Override
    public void close() {
    }

    private static final int DRAIN_BUFFER_SIZE = 8192;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[buffered=" + buffered.remaining() + " unread=" + unreadFromChannel + "]";
    }
}
