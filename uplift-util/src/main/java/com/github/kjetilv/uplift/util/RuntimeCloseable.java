package com.github.kjetilv.uplift.util;

import module java.base;

public interface RuntimeCloseable extends AutoCloseable {

    @Override
    void close();
}
