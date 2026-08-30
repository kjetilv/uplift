package com.github.kjetilv.uplift.json.io;

import module java.base;

final class StreamSink extends AbstractEncodingSink {

    private final LongAdder lengthCounter = new LongAdder();

    private final OutputStream outputStream;

    StreamSink(OutputStream outputStream, Charset charset) {
        super(charset);
        this.outputStream = Objects.requireNonNull(outputStream, "baos");
    }

    @Override
    public Mark mark() {
        var initialLength = lengthCounter.longValue();
        return () ->
            initialLength != lengthCounter.longValue();
    }

    @Override
    public void accept(String string) {
        try {
            var bytes = string.getBytes(charset());
            lengthCounter.add(bytes.length);
            outputStream.write(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write " + string, e);
        }
    }

    @Override
    public long length() {
        return Math.toIntExact(lengthCounter.longValue());
    }

    private static boolean realTrue(Boolean b) {
        return b != null && b;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[->" + outputStream + ']';
    }
}
