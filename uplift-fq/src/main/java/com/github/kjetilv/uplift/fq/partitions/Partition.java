package com.github.kjetilv.uplift.fq.partitions;

public record Partition(int no, int count, long offset, long length) {

    boolean first() {
        return no == 0;
    }

    boolean last() {
        return no == count - 1;
    }
}
