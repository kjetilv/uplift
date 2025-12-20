package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.paths.Writer;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Objects;

public class ByteBufferWriter implements Writer<byte[]> {

    private final Path path;

    private final RandomAccessFile randomAccessFile;

    private final FileChannel fileChannel;

    public ByteBufferWriter(Path path) {
        this.path = Objects.requireNonNull(path, "path");
        try {
            randomAccessFile = new RandomAccessFile(this.path.toFile(), "rw");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        fileChannel = randomAccessFile.getChannel();
    }

    @Override
    public Writer<byte[]> write(byte[] line) {
        try {
            var written = 0;
            while (written < line.length) {
                written += fileChannel.write(ByteBuffer.wrap(line));
            }
            var ln = 0;
            while (ln < 1) {
                ln += fileChannel.write(ByteBuffer.wrap("\n".getBytes()));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not write to " + path, e);
        }
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
}
