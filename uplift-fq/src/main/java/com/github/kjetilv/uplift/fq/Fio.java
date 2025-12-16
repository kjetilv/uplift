package com.github.kjetilv.uplift.fq;

public interface Fio<T> {

    T read(byte[] line);

    byte[] write(T value);

    Class<T> type();
}
