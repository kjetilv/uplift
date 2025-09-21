package com.github.kjetilv.uplift.asynchttp;

import module java.base;

public interface ChannelHandler<S extends ChannelState, C extends ChannelHandler<S, C>>
    extends CompletionHandler<Integer, S> {

    S channelState(ByteBuffer byteBuffer);

    C bind(AsynchronousByteChannel channel);
}
