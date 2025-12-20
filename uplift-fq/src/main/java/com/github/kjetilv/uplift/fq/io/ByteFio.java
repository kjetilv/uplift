package com.github.kjetilv.uplift.fq.io;

import com.github.kjetilv.uplift.fq.Fio;

public record ByteFio() implements Fio<byte[], byte[]> {

    @Override
    public byte[] read(byte[] line) {
        return line;
    }

    @Override
    public byte[] write(byte[] value) {
        return value;
    }

    @Override
    public Class<byte[]> type() {
        return byte[].class;
    }
}
