package com.github.kjetilv.uplift.fq.io;

import com.github.kjetilv.uplift.fq.Fio;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record ByteBufferStringFio(Charset cs) implements Fio<ByteBuffer, String> {

    public ByteBufferStringFio(Charset cs) {
        this.cs = cs == null ? StandardCharsets.UTF_8 : cs;
    }

    public ByteBufferStringFio() {
        this(null);
    }

    @Override
    public String read(ByteBuffer buffer) {
        return buffer.asCharBuffer().toString();
    }

    @Override
    public ByteBuffer write(String value) {
        return ByteBuffer.wrap(value.getBytes(cs));
    }

    @Override
    public Class<String> type() {
        return String.class;
    }
}
