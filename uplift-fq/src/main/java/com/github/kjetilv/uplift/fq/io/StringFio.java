package com.github.kjetilv.uplift.fq.io;

import com.github.kjetilv.uplift.fq.Fio;

public final class StringFio implements Fio<String> {

    @Override
    public String read(String line) {
        return line;
    }

    @Override
    public String write(String value) {
        return value;
    }

    @Override
    public Class<String> type() {
        return String.class;
    }
}
