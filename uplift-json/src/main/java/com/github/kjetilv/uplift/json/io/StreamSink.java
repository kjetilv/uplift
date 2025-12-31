package com.github.kjetilv.uplift.json.io;

import module java.base;

final class StreamSink implements Sink {

    private final LongAdder lengthCounter = new LongAdder();

    private final OutputStream outputStream;

    private final Charset charset;

    StreamSink(OutputStream outputStream) {
        this(outputStream, null);
    }

    StreamSink(OutputStream outputStream, Charset charset) {
        this.outputStream = Objects.requireNonNull(outputStream, "baos");
        this.charset = charset == null ? StandardCharsets.UTF_8 : charset;
    }

    @Override
    public Sink accept(String str) {
        try {
            var bytes = str.getBytes(charset);
            lengthCounter.add(bytes.length);
            outputStream.write(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write " + str, e);
        }
        return this;
    }

    @Override
    public Mark mark() {
        var initialLength = length();
        var moved = new AtomicReference<Boolean>();
        return () ->
            moved.updateAndGet(alreadyMoved ->
                truDat(alreadyMoved) || length() > initialLength);
    }

    @Override
    public int length() {
        return Math.toIntExact(lengthCounter.longValue());
    }

    private static boolean truDat(Boolean b) {
        return b != null && b;
    }

    @Override
    public String toString() {
        return "StreamSink[->" + outputStream + ']';
    }

}
