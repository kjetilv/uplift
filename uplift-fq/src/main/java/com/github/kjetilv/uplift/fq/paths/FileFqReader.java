package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqReader;

import java.util.concurrent.atomic.LongAdder;

import static java.util.Objects.requireNonNull;

final class FileFqReader<I, O> implements FqReader<O> {

    private final Fio<I, O> fio;

    private final Reader<I> reader;

    private final LongAdder count = new LongAdder();

    FileFqReader(Fio<I, O> fio, Reader<I> reader) {
        this.fio = requireNonNull(fio, "fio");
        this.reader = requireNonNull(reader, "reader");
    }

    @Override
    public O next() {
        var nextLine = reader.read();
        if (nextLine == null) {
            return null;
        }
        O o;
        try {
            o = fio.read(nextLine);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse #" + count, e);
        }
        count.increment();
        return o;
    }
}
