package com.github.kjetilv.uplift.json.io;

import java.util.concurrent.atomic.AtomicReference;

public record StringSink(StringBuilder sb) implements Sink {

    @Override
    public Sink accept(String str) {
        sb.append(str);
        return this;
    }

    @Override
    public Mark mark() {
        int length = sb.length();
        AtomicReference<Boolean> moved = new AtomicReference<>();
        return () ->
            moved.updateAndGet(alreadyMoved ->
                alreadyMoved != null && alreadyMoved || sb.length() > length);
    }

    @Override
    public int length() {
        return sb.length();
    }
}
