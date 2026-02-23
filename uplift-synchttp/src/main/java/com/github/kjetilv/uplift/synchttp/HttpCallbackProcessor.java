package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.read.HttpReqReader;
import com.github.kjetilv.uplift.synchttp.read.Segments;
import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.rere.HttpRes;
import com.github.kjetilv.uplift.synchttp.write.HttpResWriter;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

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

    @Override
    public boolean process(ReadableByteChannel in, WritableByteChannel out) {
        HttpReq httpReq;
        try {
            httpReq = new HttpReqReader(reqSegments).read(in);
        } catch (Exception e) {
            log.error("Failed to read request", e);
            new HttpResWriter(out).write(new HttpRes(500));
            return false;
        }
        if (httpReq == null) {
            return false;
        }
        var pooled = resSegments.acquire();
        try {
            var byteBuffer = pooled.segment().asByteBuffer();
            var callback = HttpResponseCallback.create(out, byteBuffer);
            httpHandler.handle(httpReq, callback);
            return !connectionClose(httpReq);
        } catch (Exception e) {
            log.error("Failed to handle request", e);
            new HttpResWriter(out).write(new HttpRes(500));
            return false;
        } finally {
            resSegments.release(pooled);
            httpReq.close();
        }
    }

    @Override
    public void close() {
        if (close) {
            arena.close();
        }
    }

    private static final MemorySegment CONNECTION =
        MemorySegment.ofArray("connection".getBytes(StandardCharsets.UTF_8));

    private static boolean connectionClose(HttpReq httpReq) {
        var connection = httpReq.headers().header(CONNECTION);
        return connection != null && connection.equalsIgnoreCase("close");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + httpHandler + "]";
    }
}
