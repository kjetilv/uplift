package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqReader;

import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

final class FileFqReader<I, O> implements FqReader<O> {

    private final Fio<I, O> fio;

    private final Reader<I> reader;

    private final LongAdder count = new LongAdder();

    FileFqReader(Fio<I, O> fio, Reader<I> reader) {
        this.fio = fio;
        this.reader = Objects.requireNonNull(reader, "newPuller");
    }

    @Override
    public O next() {
        var nextLine = reader.read();
        if (nextLine != null) {
            return nextLine(nextLine);
        }
        return null;
    }

    private O nextLine(I nextLine) {
        try {
            return fio.read(nextLine);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse #" + count, e);
        } finally {
            count.increment();
        }
    }
}
