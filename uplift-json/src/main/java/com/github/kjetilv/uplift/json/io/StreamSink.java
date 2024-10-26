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
        int initialLength = baos.size();
        AtomicReference<Boolean> moved = new AtomicReference<>();
        return () ->
                moved.updateAndGet(alreadyMoved ->
                        getABoolean(alreadyMoved, initialLength));
    }

    private Boolean getABoolean(Boolean alreadyMoved, int initialLength) {
        return truDat(alreadyMoved) || length() > initialLength ? true : null;
    }

    @Override
    public int length() {
        return baos.size();
    }

    private static boolean truDat(Boolean b) {
        return b != null;
    }
}
