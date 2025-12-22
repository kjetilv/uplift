package com.github.kjetilv.uplift.fq;

import java.util.List;
import java.util.Objects;

class SimpleFqProcessor<T> implements FqProcessor<T> {

    private final Fqs<T> fqs;

    SimpleFqProcessor(Fqs<T> fqs) {
        this.fqs = Objects.requireNonNull(fqs, "fqs");
    }

    @Override
    public List<T> process(List<T> items) {
        return items;
    }
}
