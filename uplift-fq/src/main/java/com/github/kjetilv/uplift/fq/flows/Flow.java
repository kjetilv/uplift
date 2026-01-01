package com.github.kjetilv.uplift.fq.flows;

public record Flow<T>(Name from, Name to, FqFlows.Processor<T> processor) {

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + description() + ": " + processor+"]";
    }

    public Name name() {
        return to();
    }

    public String description() {
        return from != null
            ? String.format("%s->%s", from.name(), to.name())
            : String.format("∅->%s", to.name());
    }

    public Flow<T> from(Name from) {
        return new Flow<>(from, to, processor);
    }

    boolean isFromSource() {
        return from == null;
    }
}
