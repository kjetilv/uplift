package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.bytes.ByteArrayIntsBytesSource;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class StringJsonReader<T extends Record> extends AbstractJsonReader<String, T> {

    public StringJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected BytesSource input(String source) {
        return new ByteArrayIntsBytesSource(source.getBytes(UTF_8));
    }
}
