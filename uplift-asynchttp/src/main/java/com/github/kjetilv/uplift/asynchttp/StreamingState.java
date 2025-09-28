package com.github.kjetilv.uplift.asynchttp;

import java.nio.ByteBuffer;

public interface StreamingState extends ChannelState {

    static StreamingState from(ByteBuffer byteBuffer) {
        return new DefaultStreamingState(byteBuffer);
    }

    StreamingState transferred(Long transferred);

    StreamingState error(Throwable error);

    Throwable error();

    boolean isOK();
}
