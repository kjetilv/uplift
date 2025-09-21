package com.github.kjetilv.uplift.json.events;

import module java.base;
import module uplift.json;

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
        return new StreamSink(out);
    }

    @Override
    protected byte[] result(ByteArrayOutputStream out) {
        return out.toByteArray();
    }
}
