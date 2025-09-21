package com.github.kjetilv.uplift.json.events;

import module java.base;
import module uplift.json;

abstract class AbstractJsonReader<S, T extends Record> implements JsonReader<S, T> {

    private final Function<Consumer<T>, Callbacks> callbacksInitializer;

    private final Json instance;

    protected AbstractJsonReader(Function<Consumer<T>, Callbacks> callbacksInitializer, JsonSession jsonSession) {
        this.callbacksInitializer = Objects.requireNonNull(callbacksInitializer, "callbacks");
        this.instance = jsonSession == null ? Json.instance() : Json.instance(jsonSession);
    }

    @Override
    public final T read(S source) {
        AtomicReference<T> reference = new AtomicReference<>();
        read(source, reference::set);
        return reference.get();
    }

    @Override
    public final void read(S source, Consumer<T> setter) {
        instance.parse(input(source), callbacksInitializer.apply(setter));
    }

    protected abstract BytesSource input(S source);
}
