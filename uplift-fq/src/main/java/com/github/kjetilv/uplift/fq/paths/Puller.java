package com.github.kjetilv.uplift.fq.paths;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.kjetilv.uplift.fq.paths.GzipUtils.gzipped;
import static com.github.kjetilv.uplift.fq.paths.GzipUtils.incompleteGZipHeader;
import static java.util.Objects.requireNonNull;

final class Puller {

    private final Path path;

    private final InputStream inputStream;

    private final StreamSplitter streamSplitter;

    private final AtomicBoolean opened = new AtomicBoolean();

    Puller(Path path, InputStream inputStream) {
        this(path, inputStream, 0);
    }

    Puller(Path path, InputStream inputStream, int bufferSize) {
        this.path = requireNonNull(path, "path");
        this.inputStream = requireNonNull(inputStream, "bufferedReader");
        this.streamSplitter = new StreamSplitter(inputStream, '\n', bufferSize);
    }

    byte[] pull() {
        Backoff backoff = null;
        while (true) {
            try {
                var segment = streamSplitter.next();
                opened.compareAndSet(false, true);
                return segment;
            } catch (Exception e) {
                if (!opened.get() && gzipped(path) && incompleteGZipHeader(path, e)) {
                    backoff = Backoff.sleepAndUpdate("Pull " + path.getFileName(), backoff);
                } else {
                    throw new IllegalStateException("Failed to read line", e);
                }
            }
        }
    }

    Path path() {
        return path;
    }

    void close() {
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
