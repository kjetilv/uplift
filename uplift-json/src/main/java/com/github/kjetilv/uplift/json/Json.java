package com.github.kjetilv.uplift.json;

import module java.base;

import static java.nio.charset.StandardCharsets.UTF_8;

@SuppressWarnings("unused")
public interface Json {

    Json INSTANCE = new JsonImpl();

    static Json instance() {
        return INSTANCE;
    }

    static Json instance(JsonSession jsonSession) {
        return jsonSession == null ? INSTANCE : new JsonImpl(jsonSession);
    }

    default Map<String, Object> map(InputStream source) {
        return asMap(source, read(source));
    }

    default Map<String, Object> map(MemorySegment source) {
        return asMap(source, read(source));
    }

    default Map<String, Object> map(ByteBuffer source) {
        return asMap(source, read(source));
    }

    default Map<String, Object> map(byte[] source) {
        return asMap(source, read(new String(source, UTF_8)));
    }

    default Map<String, Object> map(char[] source) {
        return asMap(source, read(source));
    }

    default Map<String, Object> map(BytesSource source) {
        return asMap(source, read(source));
    }

    default Map<String, Object> map(String source) {
        return asMap(source, read(source));
    }

    default List<?> array(String source) {
        return asList(source, read(source));
    }

    default List<?> array(BytesSource source) {
        return asList(source, read(source));
    }

    default List<?> array(InputStream source) {
        return asList(source, read(source));
    }

    default Object read(char[] bytes) {
        return read(new String(bytes));
    }

    default Object read(byte[] bytes) {
        return read(new String(bytes, UTF_8));
    }

    default Object read(String string) {
        try (
            InputStream in = new ByteArrayInputStream(
                Objects.requireNonNull(string, "string").getBytes(UTF_8))
        ) {
            return read(in);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read string of " + string.length() + " chars", e);
        }
    }

    default byte[] writeBytes(Object object) {
        return write(object).getBytes(UTF_8);
    }

    default String write(Object object) {
        try (var baos = new ByteArrayOutputStream()) {
            write(object, baos);
            return baos.toString(UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write " + object, e);
        }
    }

    Object read(InputStream inputStream);

    Object read(ByteBuffer inputStream);

    Object read(MemorySegment memorySegment);

    Object read(BytesSource bytesSource);

    void write(Object object, OutputStream outputStream);

    void write(Object object, WritableByteChannel byteChannel);

    Callbacks parse(String source, Callbacks callbacks);

    Callbacks parseMulti(String source, Callbacks callbacks);

    Callbacks parse(ByteBuffer source, Callbacks callbacks);

    Callbacks parseMulti(ByteBuffer source, Callbacks callbacks);

    Callbacks parse(InputStream source, Callbacks callbacks);

    Callbacks parseMulti(InputStream source, Callbacks callbacks);

    Callbacks parse(BytesSource bytesSource, Callbacks callbacks);

    Callbacks parseMulti(BytesSource bytesSource, Callbacks callbacks);

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object source, Object json) {
        if (json == null) {
            return Collections.emptySortedMap();
        }
        if (json instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
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
