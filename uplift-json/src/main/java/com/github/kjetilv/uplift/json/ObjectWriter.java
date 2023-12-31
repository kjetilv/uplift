package com.github.kjetilv.uplift.json;

public interface ObjectWriter<T extends Record> {

    WriteEvents write(T object, WriteEvents calls);
}
