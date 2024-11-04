package com.github.kjetilv.uplift.json;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public interface Json {

    default Map<?, ?> jsonMap(InputStream source) {
        return asMap(source, read(source));
    }

    default Map<?, ?> jsonMap(byte[] source) {
        return asMap(source, read(source));
    }

    default Map<?, ?> jsonMap(String source) {
        return asMap(source, read(source));
    }

    default List<?> jsonArray(String source) {
        return asList(source, read(source));
    }

    default List<?> jsonArray(InputStream source) {
        return asList(source, read(source));
    }

    default Object read(byte[] bytes) {
        return read(new String(bytes, StandardCharsets.UTF_8));
    }

    default byte[] writeBytes(Object object) {
        return write(object).getBytes(StandardCharsets.UTF_8);
    }

    default Object read(Reader reader) {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            return read(bufferedReader.lines()
                .collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read " + reader, e);
        }
    }

    default Object read(String string) {
        try (
            InputStream in = new ByteArrayInputStream(
                Objects.requireNonNull(string, "string").getBytes(StandardCharsets.UTF_8))
        ) {
            return read(in);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read string of " + string.length() + " chars", e);
        }
    }

    default String write(Object object) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            write(object, baos);
            return baos.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write " + object, e);
        }
    }

    Object read(InputStream inputStream);

    void write(Object object, OutputStream outputStream);

    Json INSTANCE = new JsonImpl();

    Function<Object, String> OBJECT_2_STRING = INSTANCE::write;

    BiConsumer<Object, OutputStream> OBJECT_OUT = INSTANCE::write;

    Function<String, Map<?, ?>> STRING_2_JSON_MAP = INSTANCE::jsonMap;

    Function<InputStream, Map<?, ?>> BYTES_2_JSON_MAP = INSTANCE::jsonMap;

    Function<String, List<?>> STRING_2_JSON_ARRAY = INSTANCE::jsonArray;

    private static Map<?, ?> asMap(Object source, Object json) {
        if (json == null) {
            return Collections.emptyMap();
        }
        if (json instanceof Map<?, ?> map) {
            return map;
        }
        throw new IllegalArgumentException("Not an object: " + source + " => " + json);
    }

    private static List<?> asList(Object source, Object json) {
        if (json == null) {
            return Collections.emptyList();
        }
        if (json instanceof List<?> list) {
            return list;
        }
        throw new IllegalArgumentException("Not an array: " + source + " => " + json);
    }
}
