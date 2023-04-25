package com.github.kjetilv.uplift.asynchttp;

import java.io.Closeable;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public final class ServerRunner implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(ServerRunner.class);

    public static ServerRunner create(int port, int requestBufferSize, ExecutorService executor) {
        IOServer server = AsyncIOServer.server(port, requestBufferSize, executor);
        ServerRunner runner = new ServerRunner(server);
        Runtime.getRuntime().addShutdownHook(new Thread(runner::close, "Close"));
        return runner;
    }

    private final IOServer server;

    private final AtomicBoolean closed = new AtomicBoolean();

    private ServerRunner(IOServer server) {
        this.server = requireNonNull(server, "server");
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                server.close();
            } finally {
                log.info("Shutdown: {}", Duration.ofMillis(uptimeMillis()));
            }
        }
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
