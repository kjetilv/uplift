package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.BytesSource;
import com.github.kjetilv.uplift.json.tokens.Source;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

class InputStreamJsonReader<T extends Record, C extends Callbacks> extends AbstractJsonReader<InputStream, T, C> {

    InputStreamJsonReader(Function<Consumer<T>, C> callbacks) {
        super(callbacks);
    }

    @Override
    protected Source input(InputStream source) {
        return new BytesSource(source);
    }
}
