package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.BytesSource;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Json;
import com.github.kjetilv.uplift.json.JsonReader;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

abstract class AbstractJsonReader<S, T extends Record> implements JsonReader<S, T> {

    private final Function<Consumer<T>, Callbacks> callbacksInitializer;

    protected AbstractJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer) {
        this.callbacksInitializer = Objects.requireNonNull(callbacksInitializer, "callbacks");
    }

    @Override
    public final T read(S source) {
        AtomicReference<T> reference = new AtomicReference<>();
        read(source, reference::set);
        return reference.get();
    }

    @Override
    public final void read(S source, Consumer<T> setter) {
        Json.INSTANCE.parse(input(source), callbacksInitializer.apply(setter));
    }

    protected abstract BytesSource input(S source);
}
