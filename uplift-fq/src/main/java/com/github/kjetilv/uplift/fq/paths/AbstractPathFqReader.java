package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;
import com.github.kjetilv.uplift.fq.FqReader;

import java.nio.file.Path;

abstract class AbstractPathFqReader<T>
    extends AbstractPathFq<T>
    implements FqReader<T> {

    AbstractPathFqReader(Path directory, Fio<byte[], T> fio, Tombstone<Path> tombstone) {
        super(directory, fio, tombstone);
    }

    @Override
    public boolean done() {
        return foundTombstone();
    }
}
