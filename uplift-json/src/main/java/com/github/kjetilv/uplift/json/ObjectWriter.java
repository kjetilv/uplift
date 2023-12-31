package com.github.kjetilv.uplift.json;

public interface ObjectWriter<T> {

    FieldEvents write(T object, FieldEvents calls);
}
