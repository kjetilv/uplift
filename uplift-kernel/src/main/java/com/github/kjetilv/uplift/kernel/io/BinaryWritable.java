package com.github.kjetilv.uplift.kernel.io;

import module java.base;

@FunctionalInterface
public interface BinaryWritable {

    int writeTo(DataOutput dos);
}
