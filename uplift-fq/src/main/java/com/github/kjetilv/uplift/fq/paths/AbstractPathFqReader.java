package com.github.kjetilv.uplift.fq.paths;

import com.github.kjetilv.uplift.fq.Fio;

import java.nio.file.Path;

abstract class AbstractPathFqReader<I, T>
    extends AbstractPathFq<I, T> {

    AbstractPathFqReader(Path directory, Fio<I, T> fio, Tombstone<Path> tombstone) {
        super(directory, fio, tombstone);
    }

    @Override
    public boolean done() {
        return foundTombstone();
    }
}
