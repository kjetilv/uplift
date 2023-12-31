package com.github.kjetilv.uplift.json.io;

import com.github.kjetilv.uplift.json.Callbacks;

abstract class UnsupportedCallbacks implements Callbacks {

    @Override
    public Callbacks objectStarted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Callbacks field(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Callbacks objectEnded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Callbacks arrayStarted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Callbacks string(String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <N extends Number> Callbacks number(N number) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Callbacks bool(boolean bool) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Callbacks nil() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Callbacks arrayEnded() {
        throw new UnsupportedOperationException();
    }
}
