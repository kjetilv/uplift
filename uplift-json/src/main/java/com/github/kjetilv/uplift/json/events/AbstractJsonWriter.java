package com.github.kjetilv.uplift.json.events;

import module java.base;
import com.github.kjetilv.uplift.json.JsonWriter;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;
import com.github.kjetilv.uplift.json.io.Sink;

abstract class AbstractJsonWriter<T extends Record, B, R> implements JsonWriter<R, T, B> {

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
        write(t, output(out));
    }

    protected abstract B builder();

    protected abstract Sink output(B out);

    protected abstract R result(B out);

    private void write(T t, Sink sink) {
        objectWriter.write(t, new DefaultFieldEvents(null, sink));
    }
}
