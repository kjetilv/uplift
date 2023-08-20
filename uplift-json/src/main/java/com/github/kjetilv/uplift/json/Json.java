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

    Function<String, Map<String, Object>> STRING_2_JSON_MAP = INSTANCE::jsonMap;

    Function<InputStream, Map<String, Object>> BYTES_2_JSON_MAP = INSTANCE::jsonMap;

    Function<String, List<Object>> JSON_ARRAY = INSTANCE::jsonArray;

    @SuppressWarnings("unchecked")
    default Map<String, Object> jsonMap(InputStream source) {
        Object json = read(source);
        if (json instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Not an object: " + source + " => " + json);
    }

    @SuppressWarnings("unchecked")
    default Map<String, Object> jsonMap(byte[] source) {
        Object json = read(source);
        if (json == null) {
            return Collections.emptyMap();
        }
        if (json instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Not an object: " + source.length + " bytes => " + json);
    }

    @SuppressWarnings("unchecked")
    default Map<String, Object> jsonMap(String source) {
        Object json = read(source);
        if (json == null) {
            return Collections.emptyMap();
        }
        if (json instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("Not an object: " + source + " => " + json);
    }

    @SuppressWarnings("unchecked")
    default List<Object> jsonArray(String source) {
        Object json = read(source);
        if (json instanceof List<?> list) {
            return (List<Object>) list;
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
