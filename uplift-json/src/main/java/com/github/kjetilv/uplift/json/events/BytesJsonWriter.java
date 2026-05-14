package com.github.kjetilv.uplift.json.events;

import module java.base;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.Sink;

public final class BytesJsonWriter<T extends Record, O extends OutputStream> extends AbstractJsonWriter<byte[], T, O> {

    public BytesJsonWriter(ObjectWriter<T> objectWriter, O outputStream) {
        super(objectWriter, () -> outputStream);
    }

    public BytesJsonWriter(ObjectWriter<T> objectWriter, Supplier<O> builder) {
        super(objectWriter, builder);
    }

    @Override
    protected Sink output(OutputStream out) {
        return Sink.stream(out);
    }
}
