package com.github.kjetilv.uplift.json;

import java.util.Collection;
import java.util.Collections;

public interface Callbacks {

    default Callbacks objectStarted() {
        return this;
    }

    default Callbacks field(Token token) {
        throw new IllegalStateException(this + " cannot accept field " + token);
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

    default Collection<Token> canonicalTokens() {
        return Collections.emptySet();
    }
}
