package com.github.kjetilv.uplift.asynchttp;

import module java.base;
import module uplift.asynchttp;

import static java.util.Objects.requireNonNull;

record DefaultStreamingState(ByteBuffer requestBuffer, Long transferred, Throwable error)
    implements StreamingState {

    DefaultStreamingState(ByteBuffer byteBuffer) {
        this(byteBuffer, null, null);
    }

    @Override
    public StreamingState transferred(Long transferred) {
        return new DefaultStreamingState(requestBuffer, requireNonNull(transferred, "transferred"), error);
    }

    @Override
    public StreamingState error(Throwable error) {
        return new DefaultStreamingState(requestBuffer, transferred, requireNonNull(error, "error"));
    }

    @Override
    public boolean isOK() {
        return error == null;
    }

    private static final String CLASS_NAME = StreamingState.class.getSimpleName();

    private static final String EMPTY = CLASS_NAME + "[<none>]";

    @Override
    public String toString() {
        return transferred == null
            ? EMPTY
            : CLASS_NAME + "[transferred=" + transferred + (error == null ? "" : " error=" + error) + "]";
    }
}
