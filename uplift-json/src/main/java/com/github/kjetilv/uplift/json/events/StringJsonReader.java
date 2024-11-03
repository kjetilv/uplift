package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.CharSequenceSource;
import com.github.kjetilv.uplift.json.tokens.Source;

import java.util.function.Consumer;
import java.util.function.Function;

public final class StringJsonReader<T extends Record> extends AbstractJsonReader<String, T> {

    public StringJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected Source input(String source) {
        return new CharSequenceSource(source);
    }
}
