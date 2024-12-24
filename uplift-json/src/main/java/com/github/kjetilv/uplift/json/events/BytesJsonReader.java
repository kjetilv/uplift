package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.AbstractJsonReader;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.bytes.ByteArrayBytesSource;

import java.util.function.Consumer;
import java.util.function.Function;

public final class BytesJsonReader<T extends Record> extends AbstractJsonReader<byte[], T> {

    public BytesJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        super(callbacksInitializer);
    }

    @Override
    protected BytesSource input(byte[] source) {
        return new ByteArrayBytesSource(source);
    }
}
