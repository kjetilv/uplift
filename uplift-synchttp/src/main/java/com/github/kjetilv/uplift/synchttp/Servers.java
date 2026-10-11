package com.github.kjetilv.uplift.synchttp;

import module java.base;

public final class Servers {

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
        Server.Processor processor = new HttpCallbackProcessor(handler);
        switch (create(port).run(processor)) {
            case Result.OK(Server.Open openServer) -> {
                try (openServer) {
                    return openServer.await(name);
                }
            }
            case Result.Denied<?> denied -> {
                return denied.retyped();
            }
        }
    }

    private Servers() {
    }

    private static int resolvePort(Integer port) {
        return port == null ? 0 : Math.max(0, port);
    }

    public sealed interface Result<T> {

        @SuppressWarnings("UnusedReturnValue")
        default Result<T> onOK(Consumer<T> consumer) {
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        default Result<T> onDenied(Consumer<InetSocketAddress> consumer) {
            return this;
        }

        <R> Result<R> map(Function<T, R> function);

        record OK<T>(T result) implements Result<T> {

            @Override
            public Result<T> onOK(Consumer<T> consumer) {
                consumer.accept(result);
                return this;
            }

            @Override
            public <R> Result<R> map(Function<T, R> function) {
                return new OK<>(function.apply(result));
            }
        }

        @SuppressWarnings("unchecked")
        record Denied<T>(InetSocketAddress address) implements Result<T> {

            @Override
            public Result<T> onDenied(Consumer<InetSocketAddress> consumer) {
                consumer.accept(address);
                return this;
            }

            @Override
            public <R> Result<R> map(Function<T, R> function) {
                return (Result<R>) this;
            }

            <R> Result<R> retyped() {
                return (Result<R>) this;
            }
        }
    }
}
