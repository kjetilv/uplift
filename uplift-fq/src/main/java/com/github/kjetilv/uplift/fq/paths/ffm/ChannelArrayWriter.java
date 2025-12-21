package com.github.kjetilv.uplift.fq.paths.ffm;

import java.nio.ByteBuffer;
import java.nio.file.Path;

public class ChannelArrayWriter extends ChannelWriter<byte[]> {

    public ChannelArrayWriter(Path path, byte separator) {
        super(path, new byte[] {separator});
    }

    @Override
    protected ByteBuffer byteBuffer(byte[] line) {
        return ByteBuffer.wrap(line);
    }
}
