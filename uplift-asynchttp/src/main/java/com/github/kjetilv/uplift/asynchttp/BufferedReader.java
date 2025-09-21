package com.github.kjetilv.uplift.asynchttp;

import module java.base;

public interface BufferedReader<B> extends Closeable {

    @Override
    void close();

    B buffer(int size);

    int read(B buffer);
}
