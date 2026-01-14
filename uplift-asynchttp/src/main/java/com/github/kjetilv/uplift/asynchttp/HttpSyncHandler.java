package com.github.kjetilv.uplift.asynchttp;

import module java.base;
import com.github.kjetilv.uplift.asynchttp.rere.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public class HttpSyncHandler implements SyncIOServer.Handler {

    private static final Logger log = LoggerFactory.getLogger(HttpSyncHandler.class);

    private final Server server;

    private final Arena arena;

    private final Supplier<Instant> time;

    private final int maxRequestLength;

    public HttpSyncHandler(Server server, Arena arena, Supplier<Instant> time, int maxRequestLength) {
        this.server = requireNonNull(server, "server");
        this.arena = requireNonNull(arena, "arena");
        this.time = requireNonNull(time, "time");
        this.maxRequestLength = maxRequestLength;
        if (this.maxRequestLength < 1024) {
            throw new IllegalArgumentException("Request length must be >=1kb: " + maxRequestLength);
        }
    }

    @Override
    public void run(ReadableByteChannel in, WritableByteChannel out) {
        var httpReq = new SyncHttpRequestParser(in, arena, maxRequestLength).parse();
        var response = server.handle(httpReq, time.get());
        new SyncHttpResponseWriter(out).write(response);
    }

    public interface Server {

        HttpResponse handle(HttpRequest httpReq, Instant instant);
    }
}
