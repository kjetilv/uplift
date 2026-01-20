package com.github.kjetilv.uplift.synchttp.req;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class QueryParamCallbacks implements OffsetsCallbacks {

    private final long offset;

    private final int length;

    private long paramStart;

    private long paramEnd;

    private long valueEnd;

    private final List<QueryParameter> queryParameters = new ArrayList<>();

    private final MemorySegment segment;

    private final OffsetsCallbacks awaitingParamEnd;

    private final OffsetsCallbacks awaitingValueEnd;

    QueryParamCallbacks(MemorySegment segment, long offset, int length, int start) {
        this.segment = Objects.requireNonNull(segment, "segment");
        this.offset = offset;
        this.length = length;
        this.paramStart = start;
        this.awaitingParamEnd = new AwaitingParamEnd();
        this.awaitingValueEnd = new AwaitingValueEnd(segment);
    }

    @Override
    public OffsetsCallbacks found(byte b, long offset) {
        return awaitingParamEnd.found(b, offset);
    }

    QueryParameter[] finish() {
        addQueryParameter(segment, paramStart, offset + length);
        return queryParameters.toArray(QueryParameter[]::new);
    }

    private void addQueryParameter(MemorySegment segment, long paramStart, long offset) {
        queryParameters.add(new QueryParameter(
            segment,
            paramStart,
            paramEnd,
            offset - paramEnd - 1
        ));
    }

    private String stateString() {
        return "[" + valueEnd + "/" + paramEnd + "/" + paramStart + "]";
    }

    private final class AwaitingValueEnd implements OffsetsCallbacks {

        private final MemorySegment segment;

        private AwaitingValueEnd(MemorySegment segment) {
            this.segment = segment;
        }

        @Override
        public OffsetsCallbacks found(byte b, long offset) {
            if (b == '&') {
                valueEnd = offset;
                addQueryParameter(segment, paramStart, valueEnd);
                paramStart = offset + 1;
                return awaitingParamEnd;
            }
            throw new IllegalStateException(this + ": Unexpected char: `" + (char) b + "` @ " + offset);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + stateString();
        }
    }

    private final class AwaitingParamEnd implements OffsetsCallbacks {

        @Override
        public OffsetsCallbacks found(byte b, long offset) {
            if (b == '=') {
                paramEnd = offset;
                return awaitingValueEnd;
            }
            throw new IllegalStateException(this + ": Unexpected char: `" + (char) b + "` @ " + offset);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + stateString();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + stateString();
    }
}
