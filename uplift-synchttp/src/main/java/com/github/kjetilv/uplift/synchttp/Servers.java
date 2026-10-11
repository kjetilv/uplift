package com.github.kjetilv.uplift.synchttp;

import module java.base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Servers {

    private static final Logger log = LoggerFactory.getLogger(Servers.class);

    public static Server.Init create() {
        return create(0);
    }

    public static Server.Init create(Integer port) {
        return create(new InetSocketAddress(
            InetAddress.getLoopbackAddress(),
            resolvePort(port)
        ));
    }

    public static Server.Init create(InetSocketAddress address) {
        return new DefaultServer(address);
    }

    public static Result<Server.Closed> await(
        String name,
        int port,
        HttpHandler handler
    ) {
        var processor = new HttpCallbackProcessor(handler);
        switch (create(port).run(processor)) {
            case Result.OK(Server.Open openServer) -> {
                try (openServer) {
                    return openServer.await(name);
                }
            }
            case Result.Denied(InetSocketAddress address) -> {
                log.warn("{} was denied binding to {}", name, address);
                return new Result.Denied<>(address);
            }
        }
    }

    private Servers() {
    }

    private static int resolvePort(Integer port) {
        return port == null ? 0 : Math.max(0, port);
    }

    public sealed interface Result<T> {

        <R> Result<R> map(Function<T, R> function);

        record OK<T>(T result) implements Result<T> {

            @Override
            public <R> Result<R> map(Function<T, R> function) {
                return new OK<>(function.apply(result));
            }

        }

        @SuppressWarnings("unchecked")
        record Denied<T>(InetSocketAddress address) implements Result<T> {

            @Override
            public <R> Result<R> map(Function<T, R> function) {
                return (Result<R>) this;
            }
        }
    }
}
