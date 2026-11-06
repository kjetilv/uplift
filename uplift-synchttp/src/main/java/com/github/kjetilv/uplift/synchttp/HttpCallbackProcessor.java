package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.read.HttpReqException;
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
                var resBuffer = resPooled.segment().asByteBuffer();
                Runnable sendContinue = () -> writeContinue(out);
                while (exchange(reader, resBuffer.clear(), sendContinue, in, out)) {
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
        ByteBuffer resBuffer,
        Runnable sendContinue,
        ReadableByteChannel in,
        WritableByteChannel out
    ) {
        HttpReq httpReq;
        try {
            httpReq = reader.read(in, sendContinue);
        } catch (Exception e) {
            readFailed(e, out);
            return false;
        }
        if (httpReq == null) {
            return false;
        }
        var keepAlive = keepAlive(httpReq);
        var callback = HttpResponseCallback.create(out, resBuffer, !keepAlive);
        try {
            httpHandler.handle(httpReq, callback);
            if (!callback.finish()) {
                log.error("Handler did not complete response: {}", httpReq);
                if (!callback.committed()) {
                    writeError(out, 500);
                }
                return false;
            }
            return keepAlive && (reader.reusable() || reader.drain(DRAIN_LIMIT));
        } catch (Exception e) {
            handlerFailed(httpReq, e, callback, out);
            return false;
        } finally {
            httpReq.close();
        }
    }

    private static void readFailed(Exception e, WritableByteChannel out) {
        HttpReqException.find(e).ifPresentOrElse(
            reqException -> {
                log.warn("Rejected request: {}", reqException.toString());
                writeError(out, reqException.status());
            },
            () -> {
                if (closed(e)) {
                    log.debug("Connection closed: {}", Throwables.summary(e));
                } else if (Throwables.clientFailure(e)) {
                    log.warn("Failed to read request {}, client closed connection", Throwables.summary(e));
                } else {
                    log.error("Failed to read request", e);
                    writeError(out, 500);
                }
            }
        );
    }

    private static void handlerFailed(
        HttpReq httpReq,
        Exception e,
        HttpResponseCallback callback,
        WritableByteChannel out
    ) {
        var reqException = HttpReqException.find(e);
        if (reqException.isPresent()) {
            log.warn("Rejected request {}: {}", httpReq, reqException.get().toString());
        } else if (closed(e)) {
            log.debug("Connection closed during {}: {}", httpReq, Throwables.summary(e));
            return;
        } else {
            log.error("Failed to handle request {}", httpReq, e);
        }
        if (!callback.committed()) {
            writeError(out, reqException.map(HttpReqException::status).orElse(500));
        }
    }

    /// HTTP/1.1 connections stay open unless the client asks to close. HTTP/1.0 connections close
    /// unless the client asks to keep them open.
    private static boolean keepAlive(HttpReq httpReq) {
        var connection = httpReq.headers().header(CONNECTION);
        return httpReq.reqLine().isHttp11()
            ? connection == null || !connection.equalsIgnoreCase("close")
            : connection != null && connection.equalsIgnoreCase("keep-alive");
    }

    private static boolean closed(Throwable e) {
        return Throwables.chain(e).anyMatch(ClosedChannelException.class::isInstance);
    }

    private static final MemorySegment CONNECTION =
        MemorySegment.ofArray("connection".getBytes(StandardCharsets.UTF_8));

    private static final ResHeader CONNECTION_CLOSE = new ResHeader("connection", "close");

    private static final byte[] CONTINUE = "HTTP/1.1 100 Continue\r\n\r\n".getBytes(StandardCharsets.US_ASCII);

    /// Max number of unread body bytes to read and discard, for the connection to be reused
    private static final long DRAIN_LIMIT = 64 * 1024;

    private static void writeError(WritableByteChannel out, int status) {
        new HttpResWriter(out).write(new HttpRes(status, CONNECTION_CLOSE));
    }

    private static void writeContinue(WritableByteChannel out) {
        var buffer = ByteBuffer.wrap(CONTINUE);
        try {
            while (buffer.hasRemaining()) {
                out.write(buffer);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write 100 Continue", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + httpHandler + "]";
    }
}
