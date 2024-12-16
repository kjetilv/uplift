package com.github.kjetilv.uplift.json;

public interface Callbacks {

    default Callbacks objectStarted() {
        return this;
    }

    default Callbacks field(Token.Field token) {
        return this;
    }

    default Callbacks objectEnded() {
        return this;
    }

    default Callbacks arrayStarted() {
        return this;
    }

    default Callbacks string(Token.String string) {
        return this;
    }

    default Callbacks number(Token.Number number) {
        return this;
    }

    default Callbacks bool(boolean bool) {
        return this;
    }

    default Callbacks nuul() {
        return this;
    }

    default Callbacks arrayEnded() {
        return this;
    }

    default boolean multi() {
        return false;
    }

    default Callbacks line() {
        return this;
    }

    default TokenResolver tokenResolver() {
        return null;
    }

    default boolean canonical() {
        return false;
    }
}
