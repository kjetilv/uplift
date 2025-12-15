package com.github.kjetilv.uplift.fq.paths;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.Optional;

import static com.github.kjetilv.uplift.fq.paths.GzipUtils.gzipped;
import static com.github.kjetilv.uplift.fq.paths.GzipUtils.incompleteGZipHeader;
import static java.util.Objects.requireNonNull;

record Puller(Path path, BufferedReader bufferedReader) {

    Puller {
        requireNonNull(path, "path");
        requireNonNull(bufferedReader, "bufferedReader");
    }

    Optional<String> pull() {
        Backoff backoff = null;
        while (true) {
            try {
                return Optional.ofNullable(bufferedReader.readLine());
            } catch (Exception e) {
                if (gzipped(path) && incompleteGZipHeader(path, e)) {
                    backoff = Backoff.sleepAndUpdate("Pull " + path.getFileName(), backoff);
                } else {
                    throw new IllegalStateException("Failed to read line", e);
                }
            }
        }
    }

    void close() {
        try {
            bufferedReader.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + path.getFileName() + "]";
    }
}
