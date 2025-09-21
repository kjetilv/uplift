package com.github.kjetilv.uplift.json.events;

import module java.base;
import module uplift.json;

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
