package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.JsonWriter;

import static java.nio.file.StandardOpenOption.*;

final class PathWriter<T extends Record> implements JsonWriter<Path, T, Path> {

    private final Path path;

    private final JsonWriter<?, T, WritableByteChannel> writer;

    PathWriter(
        JsonWriter<?, T, WritableByteChannel> writer,
        Path path
    ) {
        this.path = path == null ? tmp() : path;
        this.writer = writer;
    }

    @Override
    public Path write(T t) {
        return writeX(t, tmp(), WRITE);
    }

    @Override
    public Path write(T t, Path out) {
        return writeX(t, out, WRITE, CREATE_NEW);
    }

    private Path writeX(T t, Path out, OpenOption... openOptions) {
        try (
            var channel = Files.newByteChannel(out, openOptions)
        ) {
            writer.write(t, channel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    private static Path tmp() {
        try {
            return Files.createTempFile(UUID.randomUUID().toString(), ".json");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create a temp file", e);
        }
    }
}
