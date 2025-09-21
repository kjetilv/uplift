package com.github.kjetilv.uplift.json.events;

import module java.base;
import module uplift.json;

public final class InputStreamJsonReader<T extends Record> extends AbstractJsonReader<InputStream, T> {

    public InputStreamJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        this(callbacksInitializer, null);
    }

    public InputStreamJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer, JsonSession jsonSession) {
        super(callbacksInitializer, jsonSession);
    }

    @Override
    protected BytesSource input(InputStream source) {
        return new InputStreamIntsBytesSource(source);
    }
}
