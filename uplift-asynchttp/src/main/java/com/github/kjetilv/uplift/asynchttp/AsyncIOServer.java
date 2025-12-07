package com.github.kjetilv.uplift.asynchttp;

import module java.base;

import static com.github.kjetilv.uplift.asynchttp.IOServer.requestBufferSize;
import static com.github.kjetilv.uplift.asynchttp.IOServer.resolveAddress;

public interface AsyncIOServer extends IOServer {

    static AsyncIOServer create(Integer port, int requestBufferSize) {
        return new DefaultAsyncIOServer(
            resolveAddress(port),
            requestBufferSize(requestBufferSize)
        );
    }

    <S extends ChannelState, C extends AsyncChannelHandler<S, C>> AsyncIOServer run(HandlerProvider<S, C> handlerProvider);

    interface HandlerProvider<S extends ChannelState, C extends AsyncChannelHandler<S, C>> {

        AsyncChannelHandler<S, C> handler(AsynchronousByteChannel channel);
    }
}
