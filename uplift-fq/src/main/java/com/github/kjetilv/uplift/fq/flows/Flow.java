package com.github.kjetilv.uplift.fq.flows;

public record Flow<T>(Name from, Name to, FqFlows.Processor<T> processor) {

    public Name name() {
        return to();
    }

    public String description() {
        return from != null
            ? String.format("%s->%s", from.name(), to.name())
            : String.format("∅->%s", to.name());
    }

    Name fromOr(Name name) {
        return from == null ? name : from;
    }

    boolean isFromSource() {
        return from == null;
    }
}
