package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.*;
import com.github.kjetilv.uplift.json.events.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface JsonRW<T extends Record> {

    default JsonReader<String, T> stringReader() {
        return new StringJsonReader<>(callbacks());
    }

    default JsonReader<byte[], T> bytesReader() {
        return new BytesJsonReader<>(callbacks());
    }

    default JsonReader<InputStream, T> streamReader() {
        return new InputStreamJsonReader<>(callbacks());
    }

    default JsonWriter<String, T, StringBuilder> stringWriter() {
        return new StringJsonWriter<>(objectWriter());
    }

    default JsonWriter<byte[], T, ByteArrayOutputStream> bytesWriter() {
        return new BytesJsonWriter<>(objectWriter());
    }

    default JsonReader<String, T> stringReader(JsonSession jsonSession) {
        return new StringJsonReader<>(callbacks(), jsonSession);
    }

    default JsonReader<byte[], T> bytesReader(JsonSession jsonSession) {
        return new BytesJsonReader<>(callbacks(), jsonSession);
    }

    default JsonReader<InputStream, T> streamReader(JsonSession jsonSession) {
        return new InputStreamJsonReader<>(callbacks(), jsonSession);
    }

    Callbacks callbacks(Consumer<T> onDone);

    Function<Consumer<T>, Callbacks> callbacks();

    ObjectWriter<T> objectWriter();
}
