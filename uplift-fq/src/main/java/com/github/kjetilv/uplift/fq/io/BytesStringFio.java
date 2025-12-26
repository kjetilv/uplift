package com.github.kjetilv.uplift.fq.io;

import com.github.kjetilv.uplift.fq.Fio;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record BytesStringFio(Charset cs) implements Fio<byte[], String> {

    public BytesStringFio(Charset cs) {
        this.cs = cs == null ? StandardCharsets.UTF_8 : cs;
    }

    public BytesStringFio() {
        this(null);
    }

    @Override
    public String read(byte[] line) {
        return new String(line, cs);
    }

    @Override
    public byte[] write(String value) {
        return value.getBytes(cs);
    }

}
