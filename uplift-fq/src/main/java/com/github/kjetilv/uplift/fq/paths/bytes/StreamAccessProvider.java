package com.github.kjetilv.uplift.fq.paths.bytes;

import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.Tombstone;
import com.github.kjetilv.uplift.fq.paths.PathTombstone;
import com.github.kjetilv.uplift.fq.paths.Puller;
import com.github.kjetilv.uplift.fq.paths.Writer;
import com.github.kjetilv.uplift.util.GzipUtils;
import com.github.kjetilv.uplift.util.SayFiles;
import com.github.kjetilv.uplift.util.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;

public final class StreamAccessProvider implements AccessProvider<Path, byte[]> {

    private static final Logger log = LoggerFactory.getLogger(StreamAccessProvider.class);

    private final boolean gzipped;

    public StreamAccessProvider() {
        this(false);
    }

    public StreamAccessProvider(boolean gzipped) {
        this.gzipped = gzipped;
    }

    @Override
    public Puller<byte[]> puller(Path path) {
        return new StreamPuller(path, inputStream(path));
    }

    @Override
    public Writer<byte[]> writer(Path path) {
        return new StreamWriter(newBufferedWriter(path));
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

    private static final int GZIP_HEADER_SIZE = 10;

    private static GZIPInputStream gzipInputStream(Path path) {
        InputStream in = SayFiles.fileInputStream(GzipUtils.gzipFile(path, true));
        Supplier<Sleeper> sleeper = Sleeper.create(
            () -> "Unzip " + path.getFileName(),
            state ->
                log.warn("Incomplete Gzip file after {}: {}", state.duration(), path)
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
}
