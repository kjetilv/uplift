package com.github.kjetilv.uplift.asynchttp;

import module java.management;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public record SyncServerRunner(SyncIOServer server) {

    private static final Logger log = LoggerFactory.getLogger(SyncServerRunner.class);

    public static SyncServerRunner create(Integer port) {
        var server = SyncIOServer.create(port);
        try {
            return new SyncServerRunner(server);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(server::close, "Close"));
        }
    }

    public SyncServerRunner {
        requireNonNull(server, "server");
    }

    public SyncIOServer run(SyncIOServer.Handler handler) {
        try {
            return server.run(handler);
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
