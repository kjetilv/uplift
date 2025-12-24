package com.github.kjetilv.uplift.fq;

import com.github.kjetilv.uplift.fq.data.Name;

public record Flow<T>(Name from, Name to, FqProcessor<T> processor) {

    Name fromOr(Name name) {
        return from == null ? name : from;
    }

    String description() {
        return String.format("%s->%s", from.name(), to.name());
    }

    boolean isFromSource() {
        return from == null;
    }
}
