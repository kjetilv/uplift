package com.github.kjetilv.uplift.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({ "StaticMethodOnlyUsedInOneClass", "unused" })
public interface Json {

    Json INSTANCE = new JsonImpl();

    Function<Object, String> OBJECT_2_STRING = INSTANCE::write;

    BiConsumer<Object, OutputStream> OBJECT_OUT = INSTANCE::write;

    Function<String, Map<?, ?>> STRING_2_JSON_MAP = INSTANCE::jsonMap;

    Function<InputStream, Map<?, ?>> BYTES_2_JSON_MAP = INSTANCE::jsonMap;

    Function<String, List<?>> JSON_ARRAY = INSTANCE::jsonArray;

    default Map<?, ?> jsonMap(InputStream source) {
        Object json = read(source);
        if (json instanceof Map<?, ?> map) {
            return map;
        }
        throw new IllegalArgumentException("Not an object: " + source + " => " + json);
    }

    default Map<?, ?> jsonMap(byte[] source) {
        Object json = read(source);
        if (json == null) {
            return Collections.emptyMap();
        }
        if (json instanceof Map<?, ?> map) {
            return map;
        }
        throw new IllegalArgumentException("Not an object: " + source.length + " bytes => " + json);
    }

    default Map<?, ?> jsonMap(String source) {
        Object json = read(source);
        if (json == null) {
            return Collections.emptyMap();
        }
        if (json instanceof Map<?, ?> map) {
            return map;
        }
        throw new IllegalArgumentException("Not an object: " + source + " => " + json);
    }

    default List<?> jsonArray(String source) {
        Object json = read(source);
        if (json instanceof List<?> list) {
            return list;
        }
        throw new IllegalArgumentException("Not an array: " + source + " => " + json);
    }

    default Object read(byte[] source) {
        return read(new String(source, StandardCharsets.UTF_8));
    }

    Object read(InputStream source);

    Object read(String source);

    String write(Object object);

    default byte[] writeBytes(Object object) {
        return write(object).getBytes(StandardCharsets.UTF_8);
    }

    void write(Object object, OutputStream outputStream);

    default Object read(Reader in) {
        try (BufferedReader reader = new BufferedReader(in)) {
            return read(reader.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read " + in, e);
        }
    }
}
