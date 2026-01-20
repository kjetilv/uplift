package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.ByteChannelSink;
import com.github.kjetilv.uplift.json.io.Sink;

import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ChannelJsonWriter<T extends Record>
    extends AbstractJsonWriter<Void, T, WritableByteChannel> {

    private final Charset charset;

    public ChannelJsonWriter(ObjectWriter<T> objectWriter) {
        this(objectWriter, null);
    }

    public ChannelJsonWriter(ObjectWriter<T> objectWriter, Charset charset) {
        super(objectWriter);
        this.charset = charset == null ? UTF_8 : charset;
    }

    @Override
    protected Sink output(WritableByteChannel out) {
        return new ByteChannelSink(out, charset);
    }
}
