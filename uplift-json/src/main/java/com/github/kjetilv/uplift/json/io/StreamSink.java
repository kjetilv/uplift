package com.github.kjetilv.uplift.json.io;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public record StreamSink(ByteArrayOutputStream baos) implements Sink {

    @Override
    public Sink accept(String str) {
        try {
            baos.write(str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write " + str, e);
        }
        return this;
    }

    @Override
    public Mark mark() {
        int length = baos.size();
        AtomicReference<Boolean> moved = new AtomicReference<>();
        return () ->
            moved.updateAndGet(alreadyMoved ->
                alreadyMoved != null && alreadyMoved || baos.size() > length);
    }

    @Override
    public int length() {
        return baos.size();
    }
}
