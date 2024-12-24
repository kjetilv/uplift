package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.AbstractJsonReader;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.bytes.ReaderBytesSource;
import com.github.kjetilv.uplift.json.BytesSource;

import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ReaderJsonReader<T extends Record> extends AbstractJsonReader<Reader, T> {

    public ReaderJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected BytesSource input(Reader source) {
        return new ReaderBytesSource(source);
    }
}
