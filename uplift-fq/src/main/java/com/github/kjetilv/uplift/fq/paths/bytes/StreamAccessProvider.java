package com.github.kjetilv.uplift.fq.paths.bytes;

import com.github.kjetilv.uplift.fq.AccessProvider;
import com.github.kjetilv.uplift.fq.paths.Reader;
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

    private final BiConsumer<Path, Duration> onMax;

    public StreamAccessProvider(boolean gzipped, BiConsumer<Path, Duration> onMax) {
        this.gzipped = gzipped;
        this.onMax = onMax;
    }

    @Override
    public Reader<byte[]> reader(Path source) {
        return new StreamReader(source, inputStream(source));
    }

    @Override
    public Writer<byte[]> writer(Path source) {
        return new StreamWriter(newBufferedWriter(source), (byte) '\n');
    }

    private OutputStream newBufferedWriter(Path path) {
        var gzipFile = GzipUtils.gzipFile(path, gzipped);
        var out = SayFiles.newFileOutputStream(gzipFile);
        return gzipped ? GzipUtils.zip(path, out) : out;
    }

    private InputStream inputStream(Path path) {
        return gzipped
            ? gzipInputStream(path)
            : SayFiles.fileInputStream(path);
    }

    private GZIPInputStream gzipInputStream(Path path) {
        var in = SayFiles.fileInputStream(GzipUtils.gzipFile(path, true));
        var sleeper = Sleeper.deferred(
            () -> "Unzip " + path.getFileName(),
            onMax == null ? null : state ->
                onMax.accept(path, state.duration())
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
