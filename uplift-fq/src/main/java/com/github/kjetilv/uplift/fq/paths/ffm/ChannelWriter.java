package com.github.kjetilv.uplift.fq.paths.ffm;

import com.github.kjetilv.uplift.fq.paths.Writer;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ChannelWriter<T> implements Writer<T> {

    private final RandomAccessFile randomAccessFile;

    private final WritableByteChannel byteChannel;

    private final Function<T, ByteBuffer> byteBuffer;

    private final Supplier<ByteBuffer> linebreak;

    public ChannelWriter(
        RandomAccessFile randomAccessFile,
        Function<T, ByteBuffer> byteBuffer,
        Supplier<ByteBuffer> linebreak
    ) {
        this.byteBuffer = requireNonNull(byteBuffer, "byteBuffer");
        this.linebreak = requireNonNull(linebreak, "linebreak");
        this.randomAccessFile = requireNonNull(randomAccessFile, "randomAccessFile");
        this.byteChannel = this.randomAccessFile.getChannel();
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
            throw new IllegalStateException("Failed to close " + randomAccessFile, e);
        }
    }

    private void doWrite(ByteBuffer byteBuffer) {
        var written = 0;
        while (written < byteBuffer.capacity()) {
            try {
                written += byteChannel.write(byteBuffer);
            } catch (Exception e) {
                throw new IllegalStateException("Could not write to " + randomAccessFile, e);
            }
        }
    }
}
