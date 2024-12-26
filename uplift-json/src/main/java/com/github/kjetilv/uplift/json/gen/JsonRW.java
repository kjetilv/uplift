package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonReader;
import com.github.kjetilv.uplift.json.ObjectWriter;
import com.github.kjetilv.uplift.json.events.*;
import com.github.kjetilv.uplift.json.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface JsonRW<T extends Record> {

    default JsonReader<String, T> stringReader() {
        return new StringJsonReader<>(callbacks());
    }

    default JsonReader<LineSegment, T> lineSegmentReader() {
        return new LineSegmentJsonReader<>(callbacks());
    }

    default JsonReader<byte[], T> bytesReader() {
        return new BytesJsonReader<>(callbacks());
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

    Callbacks callbacks(Consumer<T> onDone);

    Function<Consumer<T>, Callbacks> callbacks();

    ObjectWriter<T> objectWriter();
}
