package com.github.kjetilv.uplift.fq.paths;

import module java.base;

sealed interface Ledge extends Comparable<Ledge> permits LedgeImpl {

    static Function<Long, Ledge> forFormat(String format) {
        return ledge -> new LedgeImpl(ledge, format);
    }

    @Override
    default int compareTo(Ledge l) {
        return Long.compare(ledge(), l.ledge());
    }

    String asSegment();

    long ledge();
}
