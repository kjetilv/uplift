package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.AbstractJsonReader;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.bytes.CharSequenceBytesSource;
import com.github.kjetilv.uplift.json.BytesSource;

import java.util.function.Consumer;
import java.util.function.Function;

public final class StringJsonReader<T extends Record> extends AbstractJsonReader<String, T> {

    public StringJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected BytesSource input(String source) {
        return new CharSequenceBytesSource(source);
    }
}
