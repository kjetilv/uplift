package com.github.kjetilv.uplift.fq.flows;

public interface Name {

    static Name of(String name) {
        return () -> name;
    }

    String name();

    default boolean isBlank() {
        return name().isBlank();
    }
}
