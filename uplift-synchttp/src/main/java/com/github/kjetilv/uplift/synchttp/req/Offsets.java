package com.github.kjetilv.uplift.synchttp.req;

import com.github.kjetilv.uplift.synchttp.util.Utils;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;

import java.lang.foreign.MemorySegment;
import java.util.Arrays;

class Offsets {

    private final MemorySegment segment;

    private final long offset;

    private final long length;

    private final byte[] bytes;

    private final VectorMask<Byte>[] masks;

    private final int[] firsts;

    @SuppressWarnings("unchecked")
    Offsets(MemorySegment segment, long offset, long length, byte... bytes) {
        this.segment = segment;
        this.offset = offset;
        this.length = length;

        this.bytes = bytes;
        this.firsts = new int[bytes.length];
        Arrays.fill(this.firsts, Utils.BYTE_VECTOR_LENGTH);
        this.masks = new VectorMask[bytes.length];
    }

    void scan(OffsetsCallbacks offsetsCallbacks) {
        OffsetsCallbacks callbacks = offsetsCallbacks;
        var walker = offset;
        refreshMasks(walker);
        while (true) {
            var min = Utils.BYTE_VECTOR_LENGTH;
            var smallestMask = NONE;
            for (int i = 0; i < bytes.length; i++) {
                firsts[i] = masks[i].firstTrue();
                if (firsts[i] < min) {
                    min = firsts[i];
                    smallestMask = i;
                }
            }
            if (smallestMask == NONE) {
                if (walker >= offset + length) {
                    return;
                }
                walker += Utils.BYTE_VECTOR_LENGTH;
                refreshMasks(walker);
            } else {
                callbacks = callbacks.found(bytes[smallestMask], walker + min);
                masks[smallestMask] = Utils.unset(masks[smallestMask], min + 1);
                firsts[smallestMask] = masks[smallestMask].firstTrue();
            }
        }
    }

    private void refreshMasks(long pos) {
        var byteVector = Utils.vectorFrom(segment, pos);
        for (int i = 0; i < bytes.length; i++) {
            masks[i] = byteVector.compare(VectorOperators.EQ, bytes[i]);
        }
        Arrays.fill(firsts, Utils.BYTE_VECTOR_LENGTH);
    }

    private static final int NONE = -1;
}
