package com.github.kjetilv.uplift.json.events;

import module java.base;
import module uplift.json;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class StringJsonReader<T extends Record> extends AbstractJsonReader<String, T> {

    public StringJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        this(callbacksInitializer, null);
    }

    public StringJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer, JsonSession jsonSession) {
        super(callbacksInitializer, jsonSession);
    }

    @Override
    protected BytesSource input(String source) {
        return new ByteArrayIntsBytesSource(source.getBytes(UTF_8));
    }
}
