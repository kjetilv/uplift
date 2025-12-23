package com.github.kjetilv.uplift.fq;

public record Flow<T>(String from, String to, FqProcessor<T> processor) {

    String fromOr(String name) {
        return from == null ? name : from;
    }

    String description() {
        return String.format("%s->%s", from, to);
    }

    boolean isFromSource() {
        return from == null;
    }
}
