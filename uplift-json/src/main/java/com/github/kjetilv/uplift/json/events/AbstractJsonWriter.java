package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;
import com.github.kjetilv.uplift.json.io.Sink;

import java.util.Objects;

public abstract class AbstractJsonWriter<T extends Record, B, R> implements JsonWriter<R, T, B> {

    private final ObjectWriter<T> objectWriter;

    public AbstractJsonWriter(ObjectWriter<T> objectWriter) {
        this.objectWriter = Objects.requireNonNull(objectWriter, "objectWriter");
    }

    @Override
    public final R write(T t) {
        B out = builder();
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
        objectWriter.write(
            t,
            new DefaultFieldEvents(null, sink)
        );
    }
}
