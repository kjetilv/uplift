package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.Sink;
import com.github.kjetilv.uplift.json.io.StreamSink;

import java.io.ByteArrayOutputStream;

public class BytesJsonWriter<T extends Record> extends AbstractJsonWriter<T, ByteArrayOutputStream, byte[]> {

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
