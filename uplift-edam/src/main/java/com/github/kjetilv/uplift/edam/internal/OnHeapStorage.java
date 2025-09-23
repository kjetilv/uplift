package com.github.kjetilv.uplift.edam.internal;

import module uplift.edam;
import module uplift.hash;

final class OnHeapStorage<K extends HashKind<K>> extends AbstractStorage<K> {

    private final Occurrence<K>[] occurrences;

    OnHeapStorage(Window window) {
        super(window);
        //noinspection unchecked
        this.occurrences = new Occurrence[window.count()];
    }

    @Override
    protected Occurrence<K> retrieveFrom(long index) {
        return occurrences[Math.toIntExact(index)];
    }

    @Override
    protected void storeTo(long index, Occurrence<K> occurrence) {
        occurrences[Math.toIntExact(index)] = occurrence;
    }
}
