package com.github.kjetilv.uplift.asynchttp;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface ChannelState {

    ByteBuffer requestBuffer();
}
