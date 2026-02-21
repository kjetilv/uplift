package com.github.kjetilv.uplift.fq.paths.ffm;

import module java.base;
import com.github.kjetilv.uplift.fq.io.LinesReader;
import com.github.kjetilv.uplift.fq.paths.Reader;

import static java.util.Objects.requireNonNull;

class ChannelReader<T> implements Reader<T> {

    private final Function<MemorySegment, T> mapper;

    private final LinesReader reader;

    ChannelReader(
        RandomAccessFile randomAccessFile,
        byte separator,
        Arena arena,
        Function<MemorySegment, T> mapper
    ) {
        this.reader = new LinesReader(randomAccessFile, separator, arena);
        this.mapper = requireNonNull(mapper, "mapper");
    }

    @Override
    public T read() {
        return reader.get() instanceof MemorySegment segment
            ? mapper.apply(segment)
            : null;
    }

    @Override
    public void close() {
        reader.close();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + reader + "]";
    }
}
