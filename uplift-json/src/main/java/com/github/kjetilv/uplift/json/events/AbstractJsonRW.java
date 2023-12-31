package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.DefaultWriteEvents;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractJsonRW<T extends Record, C extends AbstractCallbacks<?, T>>
    implements JsonReader<T>, JsonWriter<T> {

    private final Class<T> type;

    private final Function<Consumer<T>, C> newCallbacks;

    private final ObjectWriter<T> objectWriter;

    protected AbstractJsonRW(Class<T> type, Function<Consumer<T>, C> newCallbacks, ObjectWriter<T> objectWriter) {
        this.type = type;
        this.newCallbacks = newCallbacks;
        this.objectWriter = objectWriter;
    }

    @Override
    public T read(String string) {
        return extract(consumer -> read(string, consumer));
    }

    @Override
    public T read(InputStream inputStream) {
        return extract(setter -> read(inputStream, setter));
    }

    @Override
    public T read(Reader reader) {
        return extract(setter -> read(reader, setter));
    }

    @Override
    public void read(String string, Consumer<T> set) {
        Events.parse(callbacks(set), string);
    }

    @Override
    public void read(Reader reader, Consumer<T> set) {
        Events.parse(callbacks(set), reader);
    }

    @Override
    public void read(InputStream string, Consumer<T> set) {
        Events.parse(callbacks(set), string);
    }

    @Override
    public String write(T t) {
        return null;
    }

    @Override
    public byte[] bytes(T t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        objectWriter().write(t, new DefaultWriteEvents(null, new StreamSink(baos)));
        return baos.toByteArray();
    }

    @Override
    public void write(T t, StringBuilder stringBuilder) {
        objectWriter().write(t, new DefaultWriteEvents(null, new StringSink(stringBuilder)));
    }

    private ObjectWriter<T> objectWriter() {
        if (objectWriter == null) {
            throw new IllegalStateException(type + " does not support write");
        }
        return objectWriter;
    }

    private C callbacks(Consumer<T> set) {
        if (newCallbacks == null) {
            throw new IllegalStateException(type + " does not support read");
        }
        return newCallbacks.apply(set);
    }

    private static <T> T extract(Consumer<Consumer<T>> consumer) {
        AtomicReference<T> reference = new AtomicReference<>();
        Consumer<T> set = reference::set;
        consumer.accept(set);
        return reference.get();
    }
}
