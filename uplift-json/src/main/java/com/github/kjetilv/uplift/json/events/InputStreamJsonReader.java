package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.AbstractJsonReader;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.bytes.InputStreamIntsBytesSource;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

public final class InputStreamJsonReader<T extends Record> extends AbstractJsonReader<InputStream, T> {

    public InputStreamJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected BytesSource input(InputStream source) {
        return new InputStreamIntsBytesSource(source);
    }
}
