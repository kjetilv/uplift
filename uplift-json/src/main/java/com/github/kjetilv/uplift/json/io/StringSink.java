package com.github.kjetilv.uplift.json.io;

import module java.base;

record StringSink(StringBuilder sb) implements Sink {

    StringSink {
        Objects.requireNonNull(sb, "sb");
    }

    @Override
    public Sink accept(String str) {
        sb.append(str);
        return this;
    }

    @Override
    public Mark mark() {
        var length = sb.length();
        var moved = new AtomicReference<Boolean>();
        return () ->
            moved.updateAndGet(alreadyMoved ->
                alreadyMoved != null && alreadyMoved || sb.length() > length);
    }

    @Override
    public int length() {
        return sb.length();
    }
}
