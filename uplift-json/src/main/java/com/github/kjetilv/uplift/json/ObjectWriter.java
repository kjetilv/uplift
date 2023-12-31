package com.github.kjetilv.uplift.json;

public interface ObjectWriter<T> {

    WriteEvents write(T object, WriteEvents calls);
}
