package com.github.kjetilv.uplift.fq.paths.bytes;

import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.paths.PathTombstone;
import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.fq.paths.Tombstone;
import com.github.kjetilv.uplift.fq.paths.Writer;
import com.github.kjetilv.uplift.util.GzipUtils;
import com.github.kjetilv.uplift.util.SayFiles;
import com.github.kjetilv.uplift.util.Sleeper;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;

public final class StreamAccessProvider implements AccessProvider<Path, byte[]> {

    private final boolean gzipped;

    private final BiConsumer<Path, Duration> onMaxTime;

    public StreamAccessProvider(boolean gzipped) {
        this(gzipped, null);
    }

    public StreamAccessProvider(boolean gzipped, BiConsumer<Path, Duration> onMaxTime) {
        this.gzipped = gzipped;
        this.onMaxTime = onMaxTime != null
            ? onMaxTime
            : (_, _) -> {
            };
    }

    @Override
    public Puller<byte[]> puller(Path path) {
        return new StreamPuller(path, inputStream(path));
    }

    @Override
    public Writer<byte[]> writer(Path path) {
        return new StreamWriter(newBufferedWriter(path), (byte) '\n');
    }

    @Override
    public Tombstone<Path> tombstone(Path path) {
        return new PathTombstone(path.resolve("done"));
    }

    private OutputStream newBufferedWriter(Path path) {
        var gzipFile = GzipUtils.gzipFile(path, gzipped);
        var out = SayFiles.newFileOutputStream(gzipFile);
        return gzipped ? GzipUtils.zip(path, out) : out;
    }

    private InputStream inputStream(Path path) {
        if (!gzipped) {
            return SayFiles.fileInputStream(GzipUtils.gzipFile(path, gzipped));
        }
        return gzipInputStream(path);
    }

    private GZIPInputStream gzipInputStream(Path path) {
        var in = SayFiles.fileInputStream(GzipUtils.gzipFile(path, true));
        var sleeper = Sleeper.deferred(
            () -> "Unzip " + path.getFileName(),
            state ->
                onMaxTime.accept(path, state.duration())
        );
        while (SayFiles.sizeOf(path) <= GZIP_HEADER_SIZE) {
            sleeper.get().sleep();
        }
        while (true) {
            try {
                return GzipUtils.unzip(path, in);
            } catch (Exception e) {
                if (GzipUtils.incompleteGZipHeader(e)) {
                    sleeper.get().sleep();
                } else {
                    throw new IllegalStateException("Failed to unzip " + path, e);
                }
            }
        }
    }

    private static final int GZIP_HEADER_SIZE = 10;
}
