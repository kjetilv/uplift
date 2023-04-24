package com.github.kjetilv.uplift.asynchttp;

import java.nio.ByteBuffer;

public record BufferState(ByteBuffer requestBuffer) implements ChannelState {

}
