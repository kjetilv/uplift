package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.paths.Writer;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;

public class ByteBufferWriter implements Writer<byte[]> {

    private final Path path;

    private final RandomAccessFile randomAccessFile;

    private final FileChannel fileChannel;

    private final byte[] separator;

    public ByteBufferWriter(Path path, byte separator) {
        this.path = Objects.requireNonNull(path, "path");
        this.separator = new byte[] {separator};
        try {
            randomAccessFile = new RandomAccessFile(this.path.toFile(), "rw");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write to " + this.path, e);
        }
        fileChannel = randomAccessFile.getChannel();
    }

    @Override
    public Writer<byte[]> write(byte[] line) {
        doWrite(line);
        doWrite(this.separator);
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

    private void doWrite(byte[] bytes) {
        var length = bytes.length;
        var written = 0;
        while (written < length) {
            try {
                written += fileChannel.write(ByteBuffer.wrap(bytes));
            } catch (Exception e) {
                throw new IllegalStateException("Could not write to " + path, e);
            }
        }
    }
}
