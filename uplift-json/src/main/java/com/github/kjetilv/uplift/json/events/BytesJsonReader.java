package com.github.kjetilv.uplift.json.events;

import module java.base;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonSession;
import com.github.kjetilv.uplift.json.bytes.ByteArrayIntsBytesSource;

public final class BytesJsonReader<T extends Record> extends AbstractJsonReader<byte[], T> {

    public BytesJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        this(callbacksInitializer, null);
    }

    public BytesJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer, JsonSession jsonSession) {
        super(callbacksInitializer, jsonSession);
    }

    @Override
    protected BytesSource input(byte[] source) {
        return new ByteArrayIntsBytesSource(source);
    }
}
