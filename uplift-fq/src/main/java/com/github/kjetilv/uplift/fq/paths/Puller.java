package com.github.kjetilv.uplift.fq.paths;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

record Puller(Path path, BufferedReader bufferedReader) {

    Puller {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(bufferedReader, "bufferedReader");
    }

    Optional<String> pull() {
        Backoff backoff = null;
        while (true) {
            try {
                return Optional.ofNullable(bufferedReader.readLine());
            } catch (Exception e) {
                if (GzipUtils.incompleteHeader(path, e)) {
                    (backoff == null ? backoff = new Backoff() : backoff).zzz();
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
