package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.paths.Writer;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;

public abstract class ChannelWriter<T> implements Writer<T> {
    private final Path path;

    private final RandomAccessFile randomAccessFile;

    private final FileChannel fileChannel;

    private final T separator;

    public ChannelWriter(Path path, T separator) {
        this.path = Objects.requireNonNull(path, "path");
        try {
            this.randomAccessFile = new RandomAccessFile(this.path.toFile(), "rw");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write to " + this.path, e);
        }
        this.fileChannel = this.randomAccessFile.getChannel();
        this.separator = separator;
    }

    @Override
    public Writer<T> write(T line) {
        doWrite(byteBuffer(line));
        doWrite(ln(this.separator));
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

    protected void doWrite(
        ByteBuffer byteBuffer
    ) {
        var written = 0;
        while (written < byteBuffer.capacity()) {
            try {
                written += fileChannel.write(byteBuffer);
            } catch (Exception e) {
                throw new IllegalStateException("Could not write to " + path, e);
            }
        }
    }

    protected ByteBuffer ln(T line) {
        return byteBuffer(line);
    }

    protected abstract ByteBuffer byteBuffer(T line);
}
