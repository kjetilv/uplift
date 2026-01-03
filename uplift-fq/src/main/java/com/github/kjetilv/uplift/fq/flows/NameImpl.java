package com.github.kjetilv.uplift.fq.flows;

record NameImpl(String name) implements Name {

    @Override
    public String toString() {
        return name;
    }
}
