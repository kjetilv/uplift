package com.github.kjetilv.uplift.json;

public interface Callbacks {

    default Callbacks objectStarted() {
        return this;
    }

    default Callbacks field(String name) {
        throw new IllegalStateException(this + " cannot accept field " + name);
    }

    default Callbacks objectEnded() {
        return this;
    }

    default Callbacks arrayStarted() {
        return this;
    }

    default Callbacks string(String string) {
        return this;
    }

    default <N extends Number> Callbacks number(N number) {
        return this;
    }

    default Callbacks bool(boolean bool) {
        return this;
    }

    default Callbacks null_() {
        return this;
    }

    default Callbacks arrayEnded() {
        return this;
    }
}
