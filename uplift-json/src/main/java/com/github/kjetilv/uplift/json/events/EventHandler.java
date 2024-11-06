package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Token;

@FunctionalInterface
public interface EventHandler {

    default Callbacks callbacks() {
        throw new UnsupportedOperationException(this + " has no callbacks");
    }

    EventHandler handle(Token token);
}
