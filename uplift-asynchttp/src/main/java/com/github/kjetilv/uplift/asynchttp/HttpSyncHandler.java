package com.github.kjetilv.uplift.asynchttp;

import module java.base;
import com.github.kjetilv.uplift.asynchttp.rere.HttpRequest;
import com.github.kjetilv.uplift.asynchttp.rere.HttpResponse;
import com.github.kjetilv.uplift.asynchttp.rere.SyncHttpRequestReader;
import com.github.kjetilv.uplift.asynchttp.rere.SyncHttpResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class HttpSyncHandler implements SyncIOServer.Handler {

    private static final Logger log = LoggerFactory.getLogger(HttpSyncHandler.class);

    private final Server server;

    private final Arena arena;

    private final Supplier<Instant> time;

    private final int maxRequestLength;

    public HttpSyncHandler(Server server) {
        this(server, null, null, 0);
    }

    public HttpSyncHandler(
        Server server,
        Arena arena,
        Supplier<Instant> time,
        int maxRequestLength
    ) {
        this.server = requireNonNull(server, "server");
        this.arena = arena == null ? Arena.ofAuto() : arena;
        this.time = time == null ? Instant::now : time;
        this.maxRequestLength = maxRequestLength > 0 ? maxRequestLength : DEFAULT_MAX_REQUEST_LENGTH;
        if (this.maxRequestLength < 1024) {
            throw new IllegalArgumentException("Request length must be >=1kb: " + maxRequestLength);
        }
    }

    @Override
    public void run(ReadableByteChannel in, WritableByteChannel out) {
        HttpRequest httpReq;
        try {
            httpReq = new SyncHttpRequestReader(in, arena, maxRequestLength).read();
        } catch (Exception e) {
            log.error("Failed to handle request", e);
            new SyncHttpResponseWriter(out).write(new HttpResponse(500));
            return;
        }
        HttpResponse response;
        try {
            response = server.handle(httpReq, time.get());
        } catch (Exception e) {
            log.error("Failed to handle request", e);
            new SyncHttpResponseWriter(out).write(new HttpResponse(500));
            return;
        }
        try {
            new SyncHttpResponseWriter(out).write(response);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write response", e);
        }
    }

    private static final int DEFAULT_MAX_REQUEST_LENGTH = 8192;

    public interface Server {

        HttpResponse handle(HttpRequest httpReq, Instant instant);
    }
}
