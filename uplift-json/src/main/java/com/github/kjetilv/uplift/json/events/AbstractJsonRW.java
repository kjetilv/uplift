package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.JsonRW;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.io.DefaultFieldEvents;
import com.github.kjetilv.uplift.json.io.StreamSink;
import com.github.kjetilv.uplift.json.io.StringSink;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractJsonRW<
    T extends Record,
    C extends AbstractCallbacks<?, T>
    > implements JsonRW<
    T,
    C
    > {

    private final Class<T> type;

    private final Function<Consumer<T>, C> newCallbacks;

    private final ObjectWriter<T> objectWriter;

    protected AbstractJsonRW(Class<T> type, Function<Consumer<T>, C> newCallbacks, ObjectWriter<T> objectWriter) {
        this.type = type;
        this.newCallbacks = newCallbacks;
        this.objectWriter = objectWriter;
    }

    @Override
    public final JsonReader<String, T> stringReader() {
        return new JsonReader<>() {

            @Override
            public T read(String source) {
                return extract(consumer -> read(source, consumer));
            }

            @Override
            public void read(String source, Consumer<T> set) {
                Events.parse(callbacks(set), source);
            }
        };
    }

    @Override
    public final JsonReader<InputStream, T> streamReader() {
        return new JsonReader<>() {

            @Override
            public T read(InputStream source) {
                return extract(consumer -> read(source, consumer));
            }

            @Override
            public void read(InputStream source, Consumer<T> set) {
                Events.parse(callbacks(set), source);
            }
        };
    }

    @Override
    public final JsonReader<Reader, T> readerReader() {
        return new JsonReader<>() {

            @Override
            public T read(Reader source) {
                return extract(consumer -> read(source, consumer));
            }

            @Override
            public void read(Reader source, Consumer<T> set) {
                Events.parse(callbacks(set), source);
            }
        };
    }

    @Override
    public final JsonWriter<String, T, StringBuilder> stringWriter() {
        return new JsonWriter<>() {

            @Override
            public String write(T t) {
                StringBuilder out = new StringBuilder();
                write(t, out);
                return out.toString();
            }

            @Override
            public void write(T t, StringBuilder out) {
                objectWriter().write(t, new DefaultFieldEvents(null, new StringSink(out)));
            }
        };
    }

    @Override
    public final JsonWriter<byte[], T, ByteArrayOutputStream> streamWriter() {
        return new JsonWriter<>() {

            @Override
            public byte[] write(T t) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                write(t, baos);
                return baos.toByteArray();
            }

            @Override
            public void write(T t, ByteArrayOutputStream out) {
                objectWriter().write(t, new DefaultFieldEvents(null, new StreamSink(out)));
            }
        };
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
        return Objects.requireNonNull(
            newCallbacks.apply(set),
            () -> newCallbacks + " -> " + set
        );
    }

    private static <T> T extract(Consumer<Consumer<T>> consumer) {
        AtomicReference<T> reference = new AtomicReference<>();
        consumer.accept(reference::set);
        return reference.get();
    }
}
