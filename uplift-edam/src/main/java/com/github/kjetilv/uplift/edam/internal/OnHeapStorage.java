package com.github.kjetilv.uplift.edam.internal;

import com.github.kjetilv.uplift.edam.Window;
import com.github.kjetilv.uplift.edam.patterns.Occurrence;
import com.github.kjetilv.uplift.hash.HashKind;

final class OnHeapStorage<H extends HashKind<H>> extends AbstractStorage<H> {

    private final Occurrence<H>[] occurrences;

    OnHeapStorage(Window window) {
        super(window);
        //noinspection unchecked
        this.occurrences = new Occurrence[window.count()];
    }

    @Override
    protected Occurrence<H> retrieveFrom(long index) {
        return occurrences[Math.toIntExact(index)];
    }

    @Override
    protected void storeTo(long index, Occurrence<H> occurrence) {
        occurrences[Math.toIntExact(index)] = occurrence;
    }
}
