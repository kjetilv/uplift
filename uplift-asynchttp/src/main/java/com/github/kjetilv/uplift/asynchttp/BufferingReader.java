package com.github.kjetilv.uplift.asynchttp;

import module java.base;
import com.github.kjetilv.uplift.util.RuntimeCloseable;

public interface BufferingReader<B> extends RuntimeCloseable {

    B buffer(int size);

    int read(B buffer);
}
