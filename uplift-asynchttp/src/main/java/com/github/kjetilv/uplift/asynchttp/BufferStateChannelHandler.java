package com.github.kjetilv.uplift.asynchttp;

import module java.base;

@SuppressWarnings("unused")
public abstract class BufferStateChannelHandler<C extends AbstractChannelHandler<BufferState, C>>
    extends AbstractChannelHandler<BufferState, C> {

    protected BufferStateChannelHandler(AsynchronousByteChannel channel, int maxRequestLength, Supplier<Instant> time) {
        super(channel, maxRequestLength, time);
    }

    @Override
    public final BufferState channelState(ByteBuffer byteBuffer) {
        return new BufferState(byteBuffer);
    }
}
