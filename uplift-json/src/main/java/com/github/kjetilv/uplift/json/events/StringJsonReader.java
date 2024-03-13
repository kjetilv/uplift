package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.CharSequenceSource;
import com.github.kjetilv.uplift.json.tokens.Source;

import java.util.function.Consumer;
import java.util.function.Function;

class StringJsonReader<T extends Record, C extends Callbacks> extends AbstractJsonReader<String, T, C> {

    StringJsonReader(Function<Consumer<T>, C> callbacks) {
        super(callbacks);
    }

    @Override
    protected Source input(String source) {
        return new CharSequenceSource(source);
    }
}
