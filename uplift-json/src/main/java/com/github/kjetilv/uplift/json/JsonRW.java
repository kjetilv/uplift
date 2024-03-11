package com.github.kjetilv.uplift.json;

import com.github.kjetilv.uplift.json.events.JsonReader;
import com.github.kjetilv.uplift.json.events.JsonWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;

@SuppressWarnings("unused")
public interface JsonRW<T extends Record, C extends Callbacks> {

    JsonReader<String, T> stringReader();

    JsonReader<InputStream, T> streamReader();

    JsonReader<Reader, T> readerReader();

    JsonWriter<String, T, StringBuilder> stringWriter();

    JsonWriter<byte[], T, ByteArrayOutputStream> streamWriter();
}
