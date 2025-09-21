package com.github.kjetilv.uplift.asynchttp;

import module java.base;

public interface IOServer extends Closeable {

    @Override
    void close();

    void join();

    InetSocketAddress address();

    void awaitActive(Duration timeout);

    <S extends ChannelState, C extends ChannelHandler<S, C>> IOServer run(HandlerProvider<S, C> handlerProvider);

    interface HandlerProvider<S extends ChannelState, C extends ChannelHandler<S, C>> {

        ChannelHandler<S, C> handler(AsynchronousByteChannel channel);
    }
}
