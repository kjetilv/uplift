package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.BytesSource;
import com.github.kjetilv.uplift.json.tokens.Source;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public final class InputStreamJsonReader<T extends Record> extends AbstractJsonReader<InputStream, T> {

    public InputStreamJsonReader(Function<Consumer<T>, Callbacks> callbacks) {
        super(callbacks);
    }

    @Override
    protected Source input(InputStream source) {
        return new BytesSource(source);
    }
}
