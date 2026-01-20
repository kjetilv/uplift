package com.github.kjetilv.uplift.synchttp;

import module java.base;
import com.github.kjetilv.uplift.synchttp.read.HttpReqReader;
import com.github.kjetilv.uplift.synchttp.req.HttpReq;
import com.github.kjetilv.uplift.synchttp.res.HttpRes;
import com.github.kjetilv.uplift.synchttp.write.HttpResWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public final class HttpReqResProcessor implements Server.Processor {

    private static final Logger log = LoggerFactory.getLogger(HttpReqResProcessor.class);

    private final HttpHandler httpHandler;

    private final Arena arena;

    private final Supplier<Instant> time;

    private final int maxRequestLength;

    public HttpReqResProcessor(HttpHandler httpHandler) {
        this(
            httpHandler,
            null,
            null,
            0
        );
    }

    public HttpReqResProcessor(
        HttpHandler httpHandler,
        Arena arena,
        Supplier<Instant> time,
        int maxRequestLength
    ) {
        this.httpHandler = requireNonNull(httpHandler, "server");
        this.arena = arena == null ? Arena.ofAuto() : arena;
        this.time = time == null ? Instant::now : time;
        this.maxRequestLength = maxRequestLength > 0 ? maxRequestLength : DEFAULT_MAX_REQUEST_LENGTH;
        if (this.maxRequestLength < 1024) {
            throw new IllegalArgumentException("Request length must be >=1kb: " + maxRequestLength);
        }
    }

    @Override
    public void process(ReadableByteChannel in, WritableByteChannel out) {
        HttpReq httpReq;
        try {
            var reader = new HttpReqReader(arena, maxRequestLength);
            httpReq = reader.read(in);
        } catch (Exception e) {
            log.error("Failed to handle request", e);
            new HttpResWriter(out).write(new HttpRes(500));
            return;
        }
        HttpRes response;
        try {
            response = httpHandler.handle(httpReq, time.get());
        } catch (Exception e) {
            log.error("Failed to handle request", e);
            new HttpResWriter(out).write(new HttpRes(500));
            return;
        }
        try {
            new HttpResWriter(out).write(response);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write response", e);
        }
    }

    private static final int DEFAULT_MAX_REQUEST_LENGTH = 8192;

    public interface HttpHandler {

        HttpRes handle(HttpReq httpReq, Instant instant);
    }
}
