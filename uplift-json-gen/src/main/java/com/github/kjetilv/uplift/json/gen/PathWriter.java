package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.JsonWriter;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

final class PathWriter<T extends Record> implements JsonWriter<Path, T, Path> {

    private final Path path;

    private final JsonWriter<?, T, WritableByteChannel> writer;

    private final OpenOption[] openOptions;

    PathWriter(
        JsonWriter<?, T, WritableByteChannel> writer,
        Path path,
        OpenOption... openOptions
    ) {
        this.path = path == null ? tmp() : path;
        this.writer = writer;
        this.openOptions = openOptions;
    }

    @Override
    public Path write(T t) {
        return doWrite(t, path, WRITE);
    }

    @Override
    public Path write(T t, Path out) {
        return doWrite(t, out, WRITE, CREATE_NEW);
    }

    private Path doWrite(T t, Path out, OpenOption... defaults) {
        try (
            var channel = Files.newByteChannel(
                out,
                resolveOptions(defaults)
            )
        ) {
            writer.write(t, channel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    private OpenOption[] resolveOptions(OpenOption... defaults) {
        return this.openOptions.length == 0 ? defaults : this.openOptions;
    }

    private static Path tmp() {
        try {
            return Files.createTempFile(UUID.randomUUID().toString(), ".json");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create a temp file", e);
        }
    }
}
