package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqReader;

import java.nio.charset.Charset;
import java.nio.file.Path;

abstract class AbstractPathFqReader<T>
    extends AbstractPathFq<T>
    implements FqReader<T> {

    AbstractPathFqReader(Path directory, Fio<T> fio, Charset cs) {
        super(directory, fio, cs);
    }

    @Override
    public boolean done() {
        return foundTombstone();
    }
}
