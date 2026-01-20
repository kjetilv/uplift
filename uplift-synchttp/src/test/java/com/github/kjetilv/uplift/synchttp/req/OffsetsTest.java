package com.github.kjetilv.uplift.synchttp.req;

import org.junit.jupiter.api.Test;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OffsetsTest {

    @Test
    void triple() {
        var text = "-----a-b--c---------b-c------a---------           ";
        var offsets = new Offsets(
            MemorySegment.ofArray(text.getBytes()),
            2,
            32,
            (byte) 'a',
            (byte) 'b',
            (byte) 'c'
        );
        var poses = new HashMap<Character, List<Integer>>();
        var offsetsCallbacks = new OffsetsCallbacks() {

            @Override
            public OffsetsCallbacks found(byte b, long offset) {
                poses.computeIfAbsent(
                    (char) b, c ->
                        new ArrayList<>()
                ).add(Math.toIntExact(offset));
                return this;
            }
        };
        offsets.scan(offsetsCallbacks);

        assertThat(poses).containsExactly(
            Map.entry('a', List.of(5, 29)),
            Map.entry('b', List.of(7, 20)),
            Map.entry('c', List.of(10, 22))
        );
    }

}