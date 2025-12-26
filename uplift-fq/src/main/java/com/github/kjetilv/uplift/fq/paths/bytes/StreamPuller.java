package com.github.kjetilv.uplift.fq.paths.bytes;

import com.github.kjetilv.uplift.fq.paths.Puller;

import java.io.InputStream;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

public final class StreamPuller implements Puller<byte[]> {

    private final Path path;

    private final InputStream inputStream;

    private final BytesSplitter bytesSplitter;

    public StreamPuller(Path path, InputStream inputStream) {
        this(path, inputStream, 0);
    }

    public StreamPuller(Path path, InputStream inputStream, int bufferSize) {
        this.path = requireNonNull(path, "path");
        this.inputStream = requireNonNull(inputStream, "bufferedReader");
        this.bytesSplitter = new BytesSplitter(inputStream, '\n', bufferSize);
    }

    @Override
    public byte[] pull() {
        return bytesSplitter.next();
    }

    public void close() {
        try {
            inputStream.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + path.getFileName() + "]";
    }
}
