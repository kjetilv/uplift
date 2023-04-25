package com.github.kjetilv.uplift.kernel.io;

import java.io.DataOutput;

@FunctionalInterface
public interface BinaryWritable {

    int writeTo(DataOutput dos);
}
