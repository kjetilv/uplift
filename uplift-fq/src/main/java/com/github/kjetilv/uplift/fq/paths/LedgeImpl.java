package com.github.kjetilv.uplift.fq.paths;

record LedgeImpl(long ledge, String format) implements Ledge {

    @Override
    public String asSegment() {
        return String.format(format, ledge);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof LedgeImpl l && ledge == l.ledge;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(ledge);
    }
}
