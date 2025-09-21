package com.github.kjetilv.uplift.asynchttp;

import module java.base;

public record BufferState(ByteBuffer requestBuffer) implements ChannelState {

}
