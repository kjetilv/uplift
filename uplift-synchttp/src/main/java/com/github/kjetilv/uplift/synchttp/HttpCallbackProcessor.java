package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.read.HttpReqReader;
import com.github.kjetilv.uplift.synchttp.read.Segments;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.rere.HttpRes;
import com.github.kjetilv.uplift.synchttp.rere.ResHeader;
import com.github.kjetilv.uplift.synchttp.write.HttpResWriter;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import com.github.kjetilv.uplift.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/// Serves HTTP/1.1 connections with an [HttpHandler].
///
/// Each connection holds one request buffer and one response buffer from the pools, until it closes.
/// The handler must complete the response before it returns.
public final class HttpCallbackProcessor implements Server.Processor {

    private static final Logger log = LoggerFactory.getLogger(HttpCallbackProcessor.class);

    private final HttpHandler httpHandler;

    private final Segments reqSegments;

    private final Segments resSegments;

    private final boolean close;

    private final Arena arena;

    public HttpCallbackProcessor(HttpHandler httpHandler) {
        this(httpHandler, null, false);
    }

    public HttpCallbackProcessor(HttpHandler httpHandler, Arena arena, boolean close) {
        this.httpHandler = requireNonNull(httpHandler, "server");
        this.arena = arena == null ? Arena.ofAuto() : arena;
        this.close = arena != null && close;
        this.reqSegments = new Segments(this.arena);
        this.resSegments = new Segments(this.arena);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void serve(ReadableByteChannel in, WritableByteChannel out) {
        var reqPooled = reqSegments.acquire();
        try {
            var resPooled = resSegments.acquire();
            try {
                var reader = new HttpReqReader(reqPooled.segment());
                while (exchange(reader, resPooled.segment(), in, out)) {
                }
            } finally {
                resSegments.release(resPooled);
            }
        } finally {
            reqSegments.release(reqPooled);
        }
    }

    @Override
    public void close() {
        if (close) {
            arena.close();
        }
    }

    /// Reads one request and writes its response.
    ///
    /// @return True if the connection can be used for another request
    private boolean exchange(
        HttpReqReader reader,
        MemorySegment resSegment,
        ReadableByteChannel in,
        WritableByteChannel out
    ) {
        HttpReq httpReq;
        try {
            httpReq = reader.read(in);
        } catch (Exception e) {
            if (Throwables.clientFailure(e)) {
                log.warn("Failed to read request {}, client closed connection", Throwables.summary(e));
            } else {
                log.error("Failed to read request", e);
                writeError(out);
            }
            return false;
        }
        if (httpReq == null) {
            return false;
        }
        var callback = HttpResponseCallback.create(out, resSegment.asByteBuffer());
        try {
            httpHandler.handle(httpReq, callback);
            if (!callback.finish()) {
                log.error("Handler did not complete response: {}", httpReq);
                if (!callback.committed()) {
                    writeError(out);
                }
                return false;
            }
            if (bodyDone(reader, httpReq)) {
                return !connectionClose(httpReq);
            }
            lingeringClose(in);
            return false;
        } catch (Exception e) {
            log.error("Failed to handle request {}", httpReq, e);
            if (!callback.committed()) {
                writeError(out);
            }
            return false;
        } finally {
            httpReq.close();
        }
    }

    /// Makes sure the request body is off the connection, so the next request can be read.
    ///
    /// A small unread body is read and discarded. A large body, or one the client will not send before it
    /// gets a 100 Continue, is not.
    ///
    /// @return True if the connection can be used for another request
    private static boolean bodyDone(HttpReqReader reader, HttpReq httpReq) {
        return reader.reusable() || !expectsContinue(httpReq) && reader.drain(DRAIN_LIMIT);
    }

    /// Prepares to close a connection that may still have unread input.
    ///
    /// Closing a socket with unread input makes the operating system reset the connection, and the client
    /// may then lose the response. Instead, the output is closed first, and the input is read and discarded
    /// until the client closes, within a time and size limit.
    private static void lingeringClose(ReadableByteChannel in) {
        if (in instanceof SocketChannel socketChannel) {
            try {
                socketChannel.shutdownOutput();
                var socket = socketChannel.socket();
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
                log.debug("Lingering close ended: {}", Throwables.summary(e));
            }
        }
    }

    private static final MemorySegment CONNECTION =
        MemorySegment.ofArray("connection".getBytes(StandardCharsets.UTF_8));

    private static final MemorySegment EXPECT =
        MemorySegment.ofArray("expect".getBytes(StandardCharsets.UTF_8));

    private static final ResHeader CONNECTION_CLOSE = new ResHeader("connection", "close");

    /// Max number of unread body bytes to read and discard, for the connection to be reused
    private static final long DRAIN_LIMIT = 64 * 1024;

    /// Max time to discard input before closing a connection
    private static final Duration LINGER_TIME = Duration.ofSeconds(1);

    /// Max number of bytes to discard before closing a connection
    private static final long LINGER_LIMIT = 1024 * 1024;

    private static boolean expectsContinue(HttpReq httpReq) {
        var expect = httpReq.headers().header(EXPECT);
        return expect != null && expect.equalsIgnoreCase("100-continue");
    }

    private static void writeError(WritableByteChannel out) {
        new HttpResWriter(out).write(new HttpRes(500, CONNECTION_CLOSE));
    }

    private static boolean connectionClose(HttpReq httpReq) {
        var connection = httpReq.headers().header(CONNECTION);
        return connection != null && connection.equalsIgnoreCase("close");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + httpHandler + "]";
    }
}
