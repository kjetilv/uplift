package com.github.kjetilv.uplift.fq.flows;

import com.github.kjetilv.uplift.fq.data.Name;

public record Flow<T>(Name from, Name to, Processor<T> processor) {

    Name fromOr(Name name) {
        return from == null ? name : from;
    }

    public String description() {
        return String.format("%s->%s", from.name(), to.name());
    }

    boolean isFromSource() {
        return from == null;
    }
}
