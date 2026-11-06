package com.github.kjetilv.uplift.synchttp.read;

import module java.base;

/// A request body with a known length.
///
/// The first bytes may already be in the request buffer. The rest is read from the connection, and
/// never past the end of the body, so the next request on the connection is left in place.
@SuppressWarnings("StatementWithEmptyBody")
final class BodyBytes implements RequestBody {

    /// @return An empty body. It has no state, so one instance is shared.
    static BodyBytes empty() {
        return EMPTY_BODY;
    }

    private static final BodyBytes EMPTY_BODY = new BodyBytes(null, ByteBuffer.allocate(0), 0L, null);

    private final ReadableByteChannel channel;

    private final ByteBuffer buffered;

    private final int bufferedLength;

    private long unreadFromChannel;

    private Runnable sendContinue;

    /// @param channel           Connection
    /// @param buffered          Body bytes already in the request buffer
    /// @param unreadFromChannel Body bytes not yet read from the connection
    /// @param sendContinue      Sends 100 Continue, before the first read from the connection. May be null.
    BodyBytes(ReadableByteChannel channel, ByteBuffer buffered, long unreadFromChannel, Runnable sendContinue) {
        this.channel = channel;
        this.buffered = Objects.requireNonNull(buffered, "buffered");
        this.bufferedLength = buffered.remaining();
        this.unreadFromChannel = unreadFromChannel;
        this.sendContinue = unreadFromChannel > 0 ? sendContinue : null;
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
        if (sendContinue != null) {
            sendContinue.run();
            sendContinue = null;
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

    @Override
    public boolean complete() {
        return unreadFromChannel == 0;
    }

    @Override
    public boolean drain(long limit) {
        if (complete()) {
            return true;
        }
        if (awaitingContinue() || unreadFromChannel > limit) {
            return false;
        }
        buffered.position(buffered.limit());
        var discard = ByteBuffer.allocate(Math.toIntExact(Math.min(DRAIN_BUFFER_SIZE, unreadFromChannel)));
        while (unreadFromChannel > 0 && read(discard.clear()) >= 0) {
        }
        return complete();
    }

    @Override
    public boolean awaitingContinue() {
        return sendContinue != null;
    }

    @Override
    public int bufferedLength() {
        return bufferedLength;
    }

    @Override
    public ByteBuffer leftover() {
        return EMPTY;
    }

    @Override
    public boolean isOpen() {
        return buffered.hasRemaining() || unreadFromChannel > 0;
    }

    private static final int DRAIN_BUFFER_SIZE = 8192;

    private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[buffered=" + buffered.remaining() + " unread=" + unreadFromChannel + "]";
    }
}
