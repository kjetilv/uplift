package com.github.kjetilv.uplift.json;

public interface Callbacks {

    default Callbacks objectStarted() {
        return this;
    }

    default Callbacks field(String name) {
        return this;
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

    default Callbacks nil() {
        return this;
    }

    default Callbacks arrayEnded() {
        return this;
    }
}
