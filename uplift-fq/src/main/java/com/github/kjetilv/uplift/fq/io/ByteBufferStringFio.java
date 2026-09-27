package com.github.kjetilv.uplift.fq.io;

import module java.base;
import com.github.kjetilv.uplift.fq.Fio;

public record ByteBufferStringFio(Charset cs) implements Fio<ByteBuffer, String> {

    public ByteBufferStringFio() {
        this(null);
    }

    public ByteBufferStringFio(Charset cs) {
        this.cs = cs == null ? StandardCharsets.UTF_8 : cs;
    }

    @Override
    public String read(ByteBuffer buffer) {
        var bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, cs);
    }

    @Override
    public ByteBuffer write(String value) {
        return ByteBuffer.wrap(value.getBytes(cs));
    }
}
