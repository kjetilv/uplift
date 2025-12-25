package com.github.kjetilv.uplift.fq.paths.ffm;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public final class ChannelBufferWriter extends ChannelWriter<ByteBuffer> {

    private final byte separator;

    public ChannelBufferWriter(Path path, byte separator) {
        super(path, ByteBuffer.wrap(new byte[] {separator}));
        this.separator = separator;
    }

    @Override
    protected ByteBuffer ln(ByteBuffer line) {
        return ByteBuffer.wrap(new byte[] {separator});
    }

    @Override
    protected ByteBuffer byteBuffer(ByteBuffer line) {
        return line;
    }
}
