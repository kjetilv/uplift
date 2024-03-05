package com.github.kjetilv.uplift.json.events;

import java.io.InputStream;
import java.io.Reader;
import java.lang.foreign.MemorySegment;
import java.util.function.Consumer;

public interface JsonReader<T extends Record> {

    T read(String string);

    T read(InputStream inputStream);

    T read(Reader reader);

    void read(String string, Consumer<T> set);

    void read(Reader reader, Consumer<T> set);

    void read(InputStream string, Consumer<T> set);

    void read(MemorySegment memorySegment, long startIndex, long endIndex, Consumer<T> set);
}
