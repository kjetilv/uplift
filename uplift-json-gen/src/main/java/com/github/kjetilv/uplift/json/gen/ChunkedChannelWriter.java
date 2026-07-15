package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.JsonWriter;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.ChunkedTransferByteChannelSink;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;

public class ChunkedChannelWriter<T extends Record> implements JsonWriter<Void, T, WritableByteChannel> {

    private final Charset charset;

    private final int bufferSize;

    private final ObjectWriter<T> objectWriter;

    public ChunkedChannelWriter(Charset charset, int bufferSize, ObjectWriter<T> objectWriter) {
        this.charset = charset;
        this.bufferSize = bufferSize;
        this.objectWriter = objectWriter;
    }

    @Override
    public WritableByteChannel write(T t, WritableByteChannel out) {
        try (var sink = new ChunkedTransferByteChannelSink(out, charset, bufferSize)) {
            var events = new DefaultFieldEvents(sink);
            objectWriter.write(t, events);
        }
        return out;
    }
}
