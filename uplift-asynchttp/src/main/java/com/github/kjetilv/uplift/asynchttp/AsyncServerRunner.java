package com.github.kjetilv.uplift.asynchttp;

import module java.management;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public record AsyncServerRunner(AsyncIOServer server) {

    private static final Logger log = LoggerFactory.getLogger(AsyncServerRunner.class);

    public static AsyncServerRunner create(
        Integer port,
        int requestBufferSize
    ) {
        var server = AsyncIOServer.create(port, requestBufferSize);
        try {
            return new AsyncServerRunner(server);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(server::close, "Close"));
        }
    }

    public AsyncServerRunner {
        requireNonNull(server, "server");
    }

    public <S extends ChannelState, C extends AsyncChannelHandler<S, C>> AsyncIOServer run(
        AsyncChannelHandler<S, C> handler
    ) {
        try {
            return server.run(handler::bind);
        } finally {
            log.info("Startup: {}ms", uptimeMillis());
        }
    }

    private static long uptimeMillis() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    @Override
    public String toString() {
        return getClass() + "[]";
    }
}
