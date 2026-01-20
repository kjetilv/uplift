package com.github.kjetilv.uplift.json.events;

import module java.base;
import com.github.kjetilv.uplift.json.JsonWriter;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;
import com.github.kjetilv.uplift.json.io.Sink;

abstract class AbstractJsonWriter<R, T extends Record, B> implements JsonWriter<R, T, B> {

    private final ObjectWriter<T> objectWriter;

    AbstractJsonWriter(ObjectWriter<T> objectWriter) {
        this.objectWriter = Objects.requireNonNull(objectWriter, "objectWriter");
    }

    @Override
    public final R write(T t) {
        var out = builder();
        write(t, out);
        return result(out);
    }

    @Override
    public final void write(T t, B out) {
        try (var output = output(out)) {
            write(t, output);
        }
    }

    protected B builder() {
        throw new IllegalStateException(this + " does not support returned values");
    }

    protected abstract Sink output(B out);

    protected  R result(B out) {
        throw new IllegalStateException(this + " does not support returned values");
    }

    private void write(T t, Sink sink) {
        objectWriter.write(t, new DefaultFieldEvents(null, sink));
    }
}
