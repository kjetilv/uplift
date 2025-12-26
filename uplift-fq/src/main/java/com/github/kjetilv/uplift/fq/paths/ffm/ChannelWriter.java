package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.paths.Writer;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChannelWriter<T> implements Writer<T> {
    private final Path path;

    private final RandomAccessFile randomAccessFile;

    private final FileChannel fileChannel;

    private final Function<T, ByteBuffer> byteBuffer;

    private final Supplier<ByteBuffer> linebreak;

    public ChannelWriter(
        Path path,
        Function<T, ByteBuffer> byteBuffer,
        Supplier<ByteBuffer> linebreak
    ) {
        this.path = Objects.requireNonNull(path, "path");
        this.byteBuffer = Objects.requireNonNull(byteBuffer, "toByteBuffer");
        this.linebreak = Objects.requireNonNull(linebreak, "toLinebreak");
        try {
            this.randomAccessFile = new RandomAccessFile(this.path.toFile(), "rw");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write to " + this.path, e);
        }
        this.fileChannel = this.randomAccessFile.getChannel();
    }

    @Override
    public Writer<T> write(T line) {
        doWrite(byteBuffer.apply(line));
        doWrite(linebreak.get());
        return this;
    }

    @Override
    public void close() {
        try {
            fileChannel.close();
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
