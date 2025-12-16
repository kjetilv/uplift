package com.github.kjetilv.uplift.fq.paths.bytes;

import com.github.kjetilv.uplift.fq.paths.Backoff;
import com.github.kjetilv.uplift.fq.paths.Factories;
import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.fq.paths.Writer;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.github.kjetilv.uplift.fq.paths.GzipUtils.gzipped;
import static com.github.kjetilv.uplift.fq.paths.GzipUtils.incompleteGZipHeader;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public final class StreamFactories implements Factories<Path, byte[]> {

    @Override
    public Puller<byte[]> puller(Path path) {
        return new StreamPuller(path, inputStream(path));
    }

    @Override
    public Writer<byte[]> writer(Path path) {
        return new StreamWriter(bufferedWriter(path));
    }

    private static InputStream inputStream(Path path) {
        Backoff backoff = null;
        var gzipped = gzipped(path);
        while (true) {
            try {
                var in = newInputStream(path);
                return gzipped
                    ? new GZIPInputStream(in)
                    : in;
            } catch (Exception e) {
                if (gzipped && incompleteGZipHeader(path, e)) {
                    backoff = Backoff.sleepAndUpdate("Read " + path.getFileName(), backoff);
                } else {
                    throw new IllegalStateException("Could not read " + path, e);
                }
            }
        }
    }

    private static OutputStream bufferedWriter(Path path) {
        try {
            var out = newOutputStream(path, CREATE_NEW);
            return gzipped(path) ? new GZIPOutputStream(out, true) : out;
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + path, e);
        }
    }
}
