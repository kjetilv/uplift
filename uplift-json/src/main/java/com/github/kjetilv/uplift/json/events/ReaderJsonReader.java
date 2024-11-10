package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.AbstractJsonReader;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.CharsSource;
import com.github.kjetilv.uplift.json.Source;

import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ReaderJsonReader<T extends Record> extends AbstractJsonReader<Reader, T> {

    public ReaderJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected Source input(Reader source) {
        return new CharsSource(source);
    }
}
