package com.github.kjetilv.uplift.json.io;

import module java.base;

public abstract sealed class AbstractEncodingSink
    implements Sink
    permits ByteChannelSink, StreamSink {

    private final Charset charset;

    public AbstractEncodingSink(Charset charset) {
        this.charset = charset == null
            ? StandardCharsets.UTF_8
            : charset;
    }

    Charset charset() {
        return charset;
    }
}
