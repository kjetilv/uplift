package com.github.kjetilv.uplift.json.events;

import module java.base;
import com.github.kjetilv.uplift.json.JsonWriter;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;
import com.github.kjetilv.uplift.json.io.Sink;

abstract class AbstractJsonWriter<S, T extends Record, O>
    implements JsonWriter<S, T, O> {

    private final ObjectWriter<T> objectWriter;

    private final Supplier<O> builder;

    AbstractJsonWriter(ObjectWriter<T> objectWriter) {
        this(objectWriter, null);
    }

    AbstractJsonWriter(ObjectWriter<T> objectWriter, Supplier<O> builder) {
        this.objectWriter = Objects.requireNonNull(objectWriter, "objectWriter");
        this.builder = builder;
    }

    @Override
    public final O write(T t) {
        if (builder == null) {
            throw new IllegalStateException(this + " requires an output");
        }
        var out = builder.get();
        write(t, out);
        return out;
    }

    @Override
    public final O write(T t, O out) {
        try (var output = output(out)) {
            write(t, output);
        }
        return out;
    }

    protected abstract Sink output(O out);

    private void write(T t, Sink sink) {
        objectWriter.write(t, new DefaultFieldEvents(sink));
    }
}
