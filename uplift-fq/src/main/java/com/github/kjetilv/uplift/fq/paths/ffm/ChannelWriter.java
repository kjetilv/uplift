package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.paths.Writer;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ChannelWriter<T> implements Writer<T> {

    private final Path path;

    private final RandomAccessFile randomAccessFile;

    private final FileChannel fileChannel;

    private final Function<T, ByteBuffer> byteBuffer;

    private final Supplier<ByteBuffer> linebreak;

    public ChannelWriter(
        Path path,
        RandomAccessFile randomAccessFile,
        Function<T, ByteBuffer> byteBuffer,
        Supplier<ByteBuffer> linebreak
    ) {
        this.path = requireNonNull(path, "path");
        this.byteBuffer = requireNonNull(byteBuffer, "byteBuffer");
        this.linebreak = requireNonNull(linebreak, "linebreak");
        this.randomAccessFile = requireNonNull(randomAccessFile, "randomAccessFile");
        this.fileChannel = this.randomAccessFile.getChannel();
    }

    @Override
    public Writer<T> write(T line) {
        var byteBuffer = this.byteBuffer.apply(line);
        var linebreak = this.linebreak.get();
        doWrite(byteBuffer);
        doWrite(linebreak);
        return this;
    }

    @Override
    public void close() {
        try {
            randomAccessFile.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close " + path, e);
        }
    }

    private void doWrite(ByteBuffer byteBuffer) {
        var written = 0;
        while (written < byteBuffer.capacity()) {
            try {
                written += fileChannel.write(byteBuffer);
            } catch (Exception e) {
                throw new IllegalStateException("Could not write to " + path, e);
            }
        }
    }
}
