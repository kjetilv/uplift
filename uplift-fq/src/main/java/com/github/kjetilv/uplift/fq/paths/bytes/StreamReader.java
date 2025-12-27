package com.github.kjetilv.uplift.fq.paths.bytes;

import com.github.kjetilv.uplift.fq.paths.Reader;

import java.io.InputStream;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

final class StreamReader implements Reader<byte[]> {

    private final Path path;

    private final InputStream inputStream;

    private final BytesSplitter bytesSplitter;

    StreamReader(Path path, InputStream inputStream) {
        this(path, inputStream, 0);
    }

    StreamReader(Path path, InputStream inputStream, int bufferSize) {
        this.path = requireNonNull(path, "path");
        this.inputStream = requireNonNull(inputStream, "bufferedReader");
        this.bytesSplitter = new BytesSplitter(inputStream, '\n', bufferSize);
    }

    @Override
    public byte[] read() {
        return bytesSplitter.next();
    }

    @Override
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
