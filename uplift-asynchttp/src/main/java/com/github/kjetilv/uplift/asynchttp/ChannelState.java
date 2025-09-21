package com.github.kjetilv.uplift.asynchttp;

import module java.base;

@FunctionalInterface
public interface ChannelState {

    ByteBuffer requestBuffer();
}
