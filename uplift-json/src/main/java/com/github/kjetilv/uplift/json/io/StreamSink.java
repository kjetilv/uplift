package com.github.kjetilv.uplift.json.io;

import module java.base;

public record StreamSink(ByteArrayOutputStream baos) implements Sink {

    public StreamSink {
        Objects.requireNonNull(baos, "baos");
    }

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
        var initialLength = baos.size();
        var moved = new AtomicReference<Boolean>();
        return () ->
            moved.updateAndGet(alreadyMoved ->
                truDat(alreadyMoved) || length() > initialLength);
    }

    @Override
    public int length() {
        return baos.size();
    }

    private static boolean truDat(Boolean b) {
        return b != null && b;
    }
}
