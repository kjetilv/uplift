package com.github.kjetilv.uplift.fq.paths;

import java.nio.file.Path;

public interface Puller<I> {

    I pull();

    void close();

    Path path();
}
