package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Tombstone;
import com.github.kjetilv.uplift.util.SayFiles;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.Files.exists;

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
            return SayFiles.newFileOutputStream(value);
        } catch (Exception e) {
            throw new IllegalStateException("Could not write tombstone", e);
        }
    }
}
