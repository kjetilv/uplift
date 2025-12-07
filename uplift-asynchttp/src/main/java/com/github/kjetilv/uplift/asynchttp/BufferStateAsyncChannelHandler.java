package com.github.kjetilv.uplift.asynchttp;

import module java.base;

@SuppressWarnings("unused")
public abstract class BufferStateAsyncChannelHandler<C extends AbstractAsyncChannelHandler<BufferState, C>>
    extends AbstractAsyncChannelHandler<BufferState, C> {

    protected BufferStateAsyncChannelHandler(AsynchronousByteChannel channel, int maxRequestLength, Supplier<Instant> time) {
        super(channel, maxRequestLength, time);
    }

    @Override
    public final BufferState channelState(ByteBuffer byteBuffer) {
        return new BufferState(byteBuffer);
    }
}
