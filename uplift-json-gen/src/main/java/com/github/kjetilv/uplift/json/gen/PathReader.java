package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.JsonReader;

final class PathReader<T extends Record> implements JsonReader<Path, T> {

    private final LongFunction<JsonReader<ReadableByteChannel, T>> reader;

    PathReader(LongFunction<JsonReader<ReadableByteChannel, T>> reader) {
        this.reader = reader;
    }

    @Override
    public T read(Path source) {
        long size = 0;
        try {
            size = Files.size(source);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find size of " + source, e);
        }
        if (size > 2) { // Need at least {}
            try (var channel = Files.newByteChannel(source)) {
                return reader.apply(size).read(channel);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read from " + source + " (" + size + " bytes)", e);
            }
        }
        throw new IllegalArgumentException("Empty or short file (" + size + " bytes): " + source);
    }

    @Override
    public void read(Path source, Consumer<T> set) {
        set.accept(read(source));
    }
}
