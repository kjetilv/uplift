package com.github.kjetilv.uplift.fq.io;

import com.github.kjetilv.uplift.fq.Fio;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public record StringFio(Charset cs) implements Fio<String> {

    public StringFio(Charset cs) {
        this.cs = cs == null ? StandardCharsets.UTF_8 : cs;
    }

    public StringFio() {
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

    @Override
    public Class<String> type() {
        return String.class;
    }
}
