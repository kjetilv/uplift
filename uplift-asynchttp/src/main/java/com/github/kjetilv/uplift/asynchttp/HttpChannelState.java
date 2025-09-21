package com.github.kjetilv.uplift.asynchttp;

import module java.base;

public record HttpChannelState(ByteBuffer requestBuffer) implements ChannelState {

    public HttpChannelState(ByteBuffer requestBuffer) {
        this.requestBuffer = Objects.requireNonNull(requestBuffer, "requestBuffer");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + requestBuffer + "]";
    }
}
