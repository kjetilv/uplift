package com.github.kjetilv.uplift.asynchttp;

import module java.management;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public record ServerRunner(IOServer server) {

    private static final Logger log = LoggerFactory.getLogger(ServerRunner.class);

    public static ServerRunner create(
        Integer port,
        int requestBufferSize
    ) {
        var server = AsyncIOServer.server(port, requestBufferSize);
        try {
            return new ServerRunner(server);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(server::close, "Close"));
        }
    }

    public ServerRunner(IOServer server) {
        this.server = requireNonNull(server, "server");
    }

    public <S extends ChannelState, C extends ChannelHandler<S, C>> IOServer run(ChannelHandler<S, C> handler) {
        try {
            return server.run(handler::bind);
        } finally {
            log.info("Startup: {}ms", uptimeMillis());
        }
    }

    private static long uptimeMillis() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }
}
