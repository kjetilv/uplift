package com.github.kjetilv.uplift.asynchttp;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousByteChannel;
import java.util.function.Function;

public interface IOServer extends Closeable {

    @Override
    void close();
    void join();
    InetSocketAddress address();

    <S extends ChannelState, C extends ChannelHandler<S, C>> IOServer run(
        Function<? super AsynchronousByteChannel, ? extends ChannelHandler<S, C>> channelHandler
    );
}
