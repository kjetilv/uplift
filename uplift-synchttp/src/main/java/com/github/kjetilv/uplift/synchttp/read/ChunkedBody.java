package com.github.kjetilv.uplift.synchttp.read;

import module java.base;

/// A request body with `Transfer-Encoding: chunked`, decoded. See RFC 9112, section 7.1.
///
/// Decoding starts with the bytes already in the request buffer, and continues with bytes read from the
/// connection into a separate buffer. Reads from the connection may go past the end of the body. Those bytes
/// are handed back with [#leftover()].
final class ChunkedBody implements RequestBody {

    private final ReadableByteChannel channel;

    private final ByteBuffer buffered;

    private final int readBufferSize;

    private ByteBuffer fromChannel;

    private Runnable sendContinue;

    private State state = State.SIZE;

    private long chunkRemaining;

    private int trailerBytes;

    private final byte[] line = new byte[MAX_LINE];

    private int lineLength;

    /// @param channel        Connection
    /// @param buffered       Bytes in the request buffer after the headers. May include the next request.
    /// @param readBufferSize Size of the buffer for reads from the connection. Bytes read past the end of the
    ///                       body must fit in the request buffer, so this should not be larger than that.
    /// @param sendContinue   Sends 100 Continue, before the first read from the connection. May be null.
    ChunkedBody(ReadableByteChannel channel, ByteBuffer buffered, int readBufferSize, Runnable sendContinue) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.buffered = Objects.requireNonNull(buffered, "buffered");
        this.readBufferSize = readBufferSize;
        this.sendContinue = sendContinue;
    }

    @Override
    public int read(ByteBuffer dst) {
        if (!dst.hasRemaining()) {
            return state == State.DONE ? -1 : 0;
        }
        while (true) {
            switch (state) {
                case SIZE -> {
                    readLine();
                    chunkRemaining = chunkSize();
                    state = chunkRemaining == 0 ? State.TRAILERS : State.DATA;
                }
                case DATA -> {
                    var source = source();
                    var count = Math.toIntExact(Math.min(
                        chunkRemaining,
                        Math.min(dst.remaining(), source.remaining())
                    ));
                    dst.put(source.slice(source.position(), count));
                    source.position(source.position() + count);
                    chunkRemaining -= count;
                    if (chunkRemaining == 0) {
                        state = State.DATA_END;
                    }
                    return count;
                }
                case DATA_END -> {
                    readLine();
                    if (lineLength != 0) {
                        throw HttpReqException.badRequest("Missing line break after chunk");
                    }
                    state = State.SIZE;
                }
                case TRAILERS -> {
                    readLine();
                    trailerBytes += lineLength;
                    if (trailerBytes > MAX_TRAILERS) {
                        throw HttpReqException.tooLarge("Trailers exceed " + MAX_TRAILERS + " bytes");
                    }
                    if (lineLength == 0) {
                        state = State.DONE;
                    }
                }
                case DONE -> {
                    return -1;
                }
            }
        }
    }

    @Override
    public boolean complete() {
        return state == State.DONE;
    }

    @Override
    public boolean drain(long limit) {
        if (complete()) {
            return true;
        }
        if (awaitingContinue()) {
            return false;
        }
        var discard = ByteBuffer.allocate(DRAIN_BUFFER_SIZE);
        var total = 0L;
        while (!complete() && total <= limit) {
            var read = read(discard.clear());
            if (read < 0) {
                break;
            }
            total += read;
        }
        return complete();
    }

    @Override
    public boolean awaitingContinue() {
        return sendContinue != null && !complete();
    }

    @Override
    public int bufferedLength() {
        return buffered.position();
    }

    @Override
    public ByteBuffer leftover() {
        return fromChannel == null ? ByteBuffer.allocate(0) : fromChannel;
    }

    @Override
    public boolean isOpen() {
        return !complete();
    }

    /// Reads one line into [#line], without the line break
    private void readLine() {
        lineLength = 0;
        while (true) {
            var source = source();
            while (source.hasRemaining()) {
                var b = source.get();
                if (b == '\n') {
                    if (lineLength > 0 && line[lineLength - 1] == '\r') {
                        lineLength--;
                    }
                    return;
                }
                if (lineLength == MAX_LINE) {
                    throw HttpReqException.tooLarge("Chunk line exceeds " + MAX_LINE + " bytes");
                }
                line[lineLength++] = b;
            }
        }
    }

    /// @return Chunk size from [#line]. Chunk extensions, after `;`, are ignored.
    private long chunkSize() {
        var size = 0L;
        var digits = 0;
        for (var i = 0; i < lineLength; i++) {
            var b = line[i];
            if (b == ';' || b == ' ' || b == '\t') {
                break;
            }
            var digit = Character.digit(b, 16);
            if (digit < 0) {
                throw HttpReqException.badRequest("Invalid chunk size: " + lineString());
            }
            if (++digits > MAX_SIZE_DIGITS) {
                throw HttpReqException.badRequest("Chunk size too large: " + lineString());
            }
            size = size << 4 | digit;
        }
        if (digits == 0) {
            throw HttpReqException.badRequest("Missing chunk size: " + lineString());
        }
        return size;
    }

    /// @return Buffer with unread bytes: the request buffer first, then bytes read from the connection
    private ByteBuffer source() {
        if (buffered.hasRemaining()) {
            return buffered;
        }
        if (fromChannel == null) {
            fromChannel = ByteBuffer.allocate(readBufferSize).flip();
        }
        if (!fromChannel.hasRemaining()) {
            if (sendContinue != null) {
                sendContinue.run();
                sendContinue = null;
            }
            fromChannel.clear();
            int read;
            try {
                do {
                    read = channel.read(fromChannel);
                } while (read == 0);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read from " + channel, e);
            }
            fromChannel.flip();
            if (read < 0) {
                throw HttpReqException.badRequest("Connection ended inside chunked body");
            }
        }
        return fromChannel;
    }

    private String lineString() {
        return new String(line, 0, lineLength, StandardCharsets.ISO_8859_1);
    }

    private static final int MAX_LINE = 4096;

    private static final int MAX_TRAILERS = 8192;

    /// 15 hex digits fit in a positive long
    private static final int MAX_SIZE_DIGITS = 15;

    private static final int DRAIN_BUFFER_SIZE = 8192;

    private enum State {
        SIZE,
        DATA,
        DATA_END,
        TRAILERS,
        DONE
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + state + (state == State.DATA ? " " + chunkRemaining : "") + "]";
    }
}
