package com.github.kjetilv.uplift.json.events;

import module java.base;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.Sink;

public final class BytesJsonWriter<T extends Record> extends AbstractJsonWriter<T, ByteArrayOutputStream, byte[]> {

    public BytesJsonWriter(ObjectWriter<T> objectWriter) {
        super(objectWriter);
    }

    @Override
    protected ByteArrayOutputStream builder() {
        return new ByteArrayOutputStream();
    }

    @Override
    protected Sink output(ByteArrayOutputStream out) {
        return Sink.stream(out);
    }

    @Override
    protected byte[] result(ByteArrayOutputStream out) {
        return out.toByteArray();
    }
}
