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

    private final Segments segments;

    public HttpCallbackProcessor(HttpHandler httpHandler) {
        this(httpHandler, null, 0);
    }

    public HttpCallbackProcessor(HttpHandler httpHandler, Arena arena, int maxRequestLength) {
        this.httpHandler = requireNonNull(httpHandler, "server");
        this.segments = new Segments(arena);
    }

    @Override
    public boolean process(ReadableByteChannel in, WritableByteChannel out) {
        HttpReq httpReq;
        try {
            httpReq = new HttpReqReader(segments).read(in);
        } catch (Exception e) {
            log.error("Failed to read request", e);
            new HttpResWriter(out).write(new HttpRes(500));
            return false;
        }
        if (httpReq == null) {
            return false;
        }
        try {
            httpHandler.handle(
                httpReq,
                HttpResponseCallback.create(out)
            );
        } catch (Exception e) {
            log.error("Failed to handle request", e);
            new HttpResWriter(out).write(new HttpRes(500));
            return true;
        } finally {
            httpReq.close();
        }
        return !connectionClose(httpReq);
    }

    @Override
    public void close() {
        segments.close();
    }

    private static boolean connectionClose(HttpReq httpReq) {
        return httpReq.headers()
            .header("connection")
            .map("close"::equalsIgnoreCase)
            .orElse(false);
    }
}
