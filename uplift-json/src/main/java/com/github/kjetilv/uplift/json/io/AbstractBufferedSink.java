package com.github.kjetilv.uplift.json.io;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

abstract sealed class AbstractBufferedSink
    implements Sink
    permits BufferedByteChannelSink, ChunkedTransferByteChannelSink {

    public static final int DEFAULT_BUFFER_SIZE = 8192;

    private final Charset charset;

    private final int bufferSize;

    AbstractBufferedSink(Charset charset, int bufferSize) {
        this.bufferSize = bufferSize > 0 ? bufferSize : DEFAULT_BUFFER_SIZE;
        this.charset = charset == null ? UTF_8 : charset;
    }

    Charset charset() {
        return charset;
    }

    int bufferSize() {
        return bufferSize;
    }
}
