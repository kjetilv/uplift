package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.CharsSource;
import com.github.kjetilv.uplift.json.tokens.Source;

import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReaderJsonReader<T extends Record, C extends Callbacks> extends AbstractJsonReader<Reader, T, C> {

    public ReaderJsonReader(Function<Consumer<T>, C> callbacks) {
        super(callbacks);
    }

    @Override
    protected Source input(Reader source) {
        return new CharsSource(source);
    }
}
