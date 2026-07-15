package com.github.kjetilv.uplift.json.gen;

import module java.base;
import com.github.kjetilv.uplift.json.JsonWriter;

final class ChannelFileWriter<T extends Record> implements JsonWriter<Path, T, Path> {

    private final Path path;

    private final JsonWriter<?, T, WritableByteChannel> writer;

    ChannelFileWriter(
        JsonWriter<?, T, WritableByteChannel> writer,
        Path path
    ) {
        this.path = path == null ? tmp() : path;
        this.writer = writer;
    }

    @Override
    public Path write(T t) {
        return write(t, tmp());
    }

    @Override
    public Path write(T t, Path out) {
        try (
            var channel = Files.newByteChannel(out)
        ) {
            writer.write(t, channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    private static Path tmp() {
        try {
            return Files.createTempFile(UUID.randomUUID().toString(), ".json");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create a temp file", e);
        }
    }
}
