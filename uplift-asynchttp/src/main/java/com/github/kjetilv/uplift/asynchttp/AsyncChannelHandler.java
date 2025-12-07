package com.github.kjetilv.uplift.asynchttp;

import module java.base;

public interface AsyncChannelHandler<S extends ChannelState, C extends AsyncChannelHandler<S, C>>
    extends CompletionHandler<Integer, S> {

    S channelState(ByteBuffer byteBuffer);

    C bind(AsynchronousByteChannel channel);
}
