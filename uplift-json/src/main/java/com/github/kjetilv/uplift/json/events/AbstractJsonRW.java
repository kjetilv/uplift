package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.JsonRW;
import com.github.kjetilv.uplift.json.ObjectWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
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

    private final Function<Consumer<T>, C> callbacks;

    private final ObjectWriter<T> objectWriter;

    protected AbstractJsonRW(Class<T> type, Function<Consumer<T>, C> callbacks, ObjectWriter<T> objectWriter) {
        this.type = type;
        this.callbacks = callbacks;
        this.objectWriter = objectWriter;
    }

    @Override
    public final JsonReader<String, T> stringReader() {
        return new StringJsonReader<>(callbacks);
    }

    @Override
    public final JsonReader<InputStream, T> streamReader() {
        return new InputStreamJsonReader<>(callbacks);
    }

    @Override
    public final JsonReader<Reader, T> readerReader() {
        return new ReaderJsonReader<>(callbacks);
    }

    @Override
    public final JsonWriter<String, T, StringBuilder> stringWriter() {
        return new StringJsonWriter<>(objectWriter);
    }

    @Override
    public final JsonWriter<byte[], T, ByteArrayOutputStream> streamWriter() {
        return new OutputStreamJsonWriter<>(objectWriter);
    }

}
