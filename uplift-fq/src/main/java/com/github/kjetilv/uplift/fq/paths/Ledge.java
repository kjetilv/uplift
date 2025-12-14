package com.github.kjetilv.uplift.fq.paths;

public interface Ledge extends Comparable<Ledge> {

    String segment();

    long no();

    @Override
    default int compareTo(Ledge l) {
        return Long.compare(no(), l.no());
    }
}
