package com.github.kjetilv.uplift.json.events;

public interface JsonWriter<T extends Record> {

    String write(T t);

    byte[] bytes(T t);

    void write(T t, StringBuilder stringBuilder);
}
