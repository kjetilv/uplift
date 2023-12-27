package com.github.kjetilv.uplift.json.events;

import java.io.InputStream;
import java.io.Reader;
import java.util.function.Consumer;

public interface JsonFactory<T> {

    T read(String string);

    T read(InputStream inputStream);

    T read(Reader reader);

    void read(String string, Consumer<T> set);

    void read(Reader reader, Consumer<T> set);

    void read(InputStream string, Consumer<T> set);
}
