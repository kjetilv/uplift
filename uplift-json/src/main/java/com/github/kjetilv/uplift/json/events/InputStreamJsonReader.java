package com.github.kjetilv.uplift.json.events;

import module java.base;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonSession;
import com.github.kjetilv.uplift.json.bytes.InputStreamIntsBytesSource;

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
