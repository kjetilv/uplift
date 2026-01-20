package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.BufferedByteChannelSink;
import com.github.kjetilv.uplift.json.io.Sink;

import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BufferedChannelJsonWriter<T extends Record>
    extends AbstractJsonWriter<Void, T, WritableByteChannel> {

    private final Charset charset;

    private final int bufferSize;

    public BufferedChannelJsonWriter(
        ObjectWriter<T> objectWriter,
        Charset charset,
        int bufferSize
    ) {
        super(objectWriter);
        this.charset = charset == null ? UTF_8 : charset;
        this.bufferSize = bufferSize;
    }

    @Override
    protected Sink output(WritableByteChannel out) {
        return new BufferedByteChannelSink(out, charset, bufferSize);
    }
}
