package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.events.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface JsonRW<T extends Record> {

    Callbacks callbacks(Consumer<T> onDone);

    Function<Consumer<T>, Callbacks> callbacks();

    ObjectWriter<T> objectWriter();

    default JsonReader<String, T> stringReader() {
        return new StringJsonReader<>(callbacks());
    }

    default JsonReader<InputStream, T> streamReader() {
        return new InputStreamJsonReader<>(callbacks());
    }

    default JsonReader<Reader, T> readerReader() {
        return new ReaderJsonReader<>(callbacks());
    }

    default JsonWriter<String, T, StringBuilder> stringWriter() {
        return new StringJsonWriter<>(objectWriter());
    }

    default JsonWriter<byte[], T, ByteArrayOutputStream> bytesWriter() {
        return new BytesJsonWriter<>(objectWriter());
    }
}
