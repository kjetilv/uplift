package com.github.kjetilv.uplift.asynchttp;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.time.Instant;
import java.util.function.Supplier;

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
