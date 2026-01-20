package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.read.HttpReqReader;
import com.github.kjetilv.uplift.synchttp.req.HttpReq;
import com.github.kjetilv.uplift.synchttp.res.HttpRes;
import com.github.kjetilv.uplift.synchttp.write.HttpResWriter;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public final class HttpCallbackProcessor implements Server.Processor {

    private static final Logger log = LoggerFactory.getLogger(HttpCallbackProcessor.class);

    private final HttpHandler httpHandler;

    private final Arena arena;

    private final int maxRequestLength;

    public HttpCallbackProcessor(HttpHandler httpHandler) {
        this(httpHandler, null, 0);
    }

    public HttpCallbackProcessor(HttpHandler httpHandler, Arena arena, int maxRequestLength) {
        this.httpHandler = requireNonNull(httpHandler, "server");
        this.arena = arena == null ? Arena.ofAuto() : arena;
        this.maxRequestLength = maxRequestLength > 0 ? maxRequestLength : DEFAULT_MAX_REQUEST_LENGTH;
        if (this.maxRequestLength < 1024) {
            throw new IllegalArgumentException("Request length must be >=1kb: " + maxRequestLength);
        }
    }

    @Override
    public void process(ReadableByteChannel in, WritableByteChannel out) {
        HttpReq httpReq;
        try {
            httpReq = new HttpReqReader(arena, maxRequestLength).read(in);
        } catch (Exception e) {
            log.error("Failed to handle request", e);
            new HttpResWriter(out).write(new HttpRes(500));
            return;
        }
        try {
            httpHandler.handle(
                httpReq,
                HttpResponseCallback.create(out));
        } catch (Exception e) {
            log.error("Failed to handle request", e);
            new HttpResWriter(out).write(new HttpRes(500));
        }
    }

    private static final int DEFAULT_MAX_REQUEST_LENGTH = 8192;

    public interface HttpHandler {

        void handle(HttpReq httpReq, HttpResponseCallback callback);
    }
}
