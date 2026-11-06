package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// One accepted connection. Records when a blocking read started, so that a read that waits too long
/// can be stopped by closing the connection from another thread. A blocking [SocketChannel] read does not
/// support timeouts.
final class Connection implements ByteChannel, GatheringByteChannel {

    private static final Logger log = LoggerFactory.getLogger(Connection.class);

    private final SocketChannel channel;

    /// [System#nanoTime()] when the current read started, or 0 if no read is in progress
    private volatile long readStart;

    private volatile boolean timedOut;

    private boolean endOfInput;

    Connection(SocketChannel channel) {
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        readStart = System.nanoTime() | 1L;
        try {
            var read = channel.read(dst);
            if (read < 0) {
                endOfInput = true;
            }
            return read;
        } finally {
            readStart = 0L;
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return channel.write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return channel.write(srcs, offset, length);
    }

    @Override
    public long write(ByteBuffer[] srcs) throws IOException {
        return channel.write(srcs);
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (Exception e) {
            log.debug("Failed to close {}: {}", channel, Throwables.summary(e));
        }
    }

    /// Closes the connection if a read has waited longer than the timeout
    ///
    /// @param now     [System#nanoTime()]
    /// @param timeout Timeout in nanoseconds
    void closeIfTimedOut(long now, long timeout) {
        var start = readStart;
        if (start != 0L && now - start > timeout) {
            timedOut = true;
            close();
        }
    }

    /// Closes the connection if it is waiting for input
    void closeIfReading() {
        if (readStart != 0L) {
            close();
        }
    }

    /// Closes the connection.
    ///
    /// If the client may still be sending, the output is closed first, and input is read and discarded until
    /// the client closes, within a time and size limit. Closing a socket with unread input makes the operating
    /// system reset the connection, and the client may then lose the last response.
    void finish() {
        try {
            if (channel.isOpen() && !endOfInput && !timedOut) {
                linger();
            }
        } finally {
            close();
        }
    }

    boolean timedOut() {
        return timedOut;
    }

    private void linger() {
        try {
            channel.shutdownOutput();
            var socket = channel.socket();
            socket.setSoTimeout(Math.toIntExact(LINGER_TIME.toMillis()));
            var input = socket.getInputStream();
            var discard = new byte[8192];
            var deadline = System.nanoTime() + LINGER_TIME.toNanos();
            var total = 0L;
            while (total < LINGER_LIMIT && System.nanoTime() < deadline) {
                var read = input.read(discard);
                if (read < 0) {
                    return;
                }
                total += read;
            }
        } catch (Exception e) {
            log.debug("Lingering close of {} ended: {}", channel, Throwables.summary(e));
        }
    }

    /// Max time to discard input before closing
    private static final Duration LINGER_TIME = Duration.ofSeconds(1);

    /// Max number of bytes to discard before closing
    private static final long LINGER_LIMIT = 1024 * 1024;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + channel + (timedOut ? " TIMED OUT" : "") + "]";
    }
}
