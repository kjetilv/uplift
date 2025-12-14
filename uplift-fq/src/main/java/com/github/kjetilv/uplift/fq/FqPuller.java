package com.github.kjetilv.uplift.fq;

import java.util.Optional;

public interface FqPuller<T> extends FqReader<T> {

    Optional<T> next();
}
