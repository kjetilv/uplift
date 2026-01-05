package com.github.kjetilv.uplift.fq.io;

import com.github.kjetilv.uplift.fq.Fio;

public record IdentityFio<T>() implements Fio<T,T> {

    @Override
    public T read(T line) {
        return line;
    }

    @Override
    public T write(T value) {
        return value;
    }
}
