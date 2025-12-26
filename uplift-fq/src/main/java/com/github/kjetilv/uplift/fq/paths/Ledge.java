package com.github.kjetilv.uplift.fq.paths;

interface Ledge extends Comparable<Ledge> {

    @Override
    default int compareTo(Ledge l) {
        return Long.compare(ledge(), l.ledge());
    }

    String asSegment();

    long ledge();
}
