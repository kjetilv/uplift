package com.github.kjetilv.uplift.fq.paths;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public record PathTombstone(Path value) implements Tombstone<Path> {

    public PathTombstone {
        Objects.requireNonNull(value, "value");
    }

    @Override
    public boolean isTombstone(Path value) {
        return value.equals(this.value);
    }

    @Override
    public boolean isSet() {
        return exists(value);
    }

    @Override
    public void set(String inscription) {
        try (
            var tombstoneWriter = tombstoneOutputStream();
            var bufferedWriter = new BufferedWriter(new OutputStreamWriter(tombstoneWriter))
        ) {
            bufferedWriter.write(inscription);
        } catch (Exception e) {
            throw new IllegalStateException("Could not set tombstone: " + value, e);
        }
    }

    private OutputStream tombstoneOutputStream() {
        try {
            return newOutputStream(value, CREATE_NEW);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write tombstone", e);
        }
    }
}
