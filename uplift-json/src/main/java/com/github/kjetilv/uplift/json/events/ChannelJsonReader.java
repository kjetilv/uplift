package com.github.kjetilv.uplift.json.events;

import module java.base;
import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonSession;
import com.github.kjetilv.uplift.json.bytes.ChannelIntSource;

public final class ChannelJsonReader<T extends Record> extends AbstractJsonReader<ReadableByteChannel, T> {

    private final long length;

    public ChannelJsonReader(
        Function<Consumer<T>, Callbacks> callbacksInitializer,
        long length
    ) {
        this(callbacksInitializer, null, length);
    }

    public ChannelJsonReader(
        Function<Consumer<T>, Callbacks> callbacksInitializer,
        JsonSession jsonSession,
        long length
    ) {
        super(callbacksInitializer, jsonSession);
        if (length < 0) {
            throw new IllegalArgumentException("len must be >= 0: " + length);
        }
        this.length = length;
    }

    @Override
    protected BytesSource input(ReadableByteChannel source) {
        return new ChannelIntSource(source, length);
    }
}
