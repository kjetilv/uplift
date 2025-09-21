package com.github.kjetilv.uplift.asynchttp;

import module java.management;
import module uplift.flogs;
import org.slf4j.Logger;

import static java.util.Objects.requireNonNull;

public record ServerRunner(IOServer server) {

    private static final Logger log = LoggerFactory.getLogger(ServerRunner.class);

    public static ServerRunner create(
        Integer port,
        int requestBufferSize
    ) {
        IOServer server = AsyncIOServer.server(
            port,
            requestBufferSize
        );
        ServerRunner runner = new ServerRunner(server);
        Runtime.getRuntime().addShutdownHook(new Thread(server::close, "Close"));
        return runner;
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
