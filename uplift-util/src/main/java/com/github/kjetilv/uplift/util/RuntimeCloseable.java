package com.github.kjetilv.uplift.util;

import java.io.Closeable;

public interface RuntimeCloseable extends Closeable {

    @Override
    void close();
}
