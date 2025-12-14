package com.github.kjetilv.uplift.fq;

public interface Fio<T> {

    T read(String line);

    String write(T value);

    Class<T> type();
}
