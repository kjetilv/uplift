package com.github.kjetilv.uplift.json.events;

import java.util.function.Function;

import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.TokenType;

abstract class AbstractEventHandler implements EventHandler {

    private final AbstractEventHandler scope;

    private final Events.Callbacks callbacks;

    AbstractEventHandler(AbstractEventHandler scope, Events.Callbacks callbacks) {
        this.scope = scope;
        this.callbacks = callbacks;
    }

    final Events.Callbacks getCallbacks() {
        return callbacks;
    }

    protected abstract AbstractEventHandler with(Events.Callbacks callbacks);

    protected final AbstractEventHandler exit() {
        return scope == null ? this : scope.with(callbacks);
    }

    protected final AbstractEventHandler exit(Function<Events.Callbacks, Events.Callbacks> action) {
        return scope == null ? this : scope.with(action.apply(callbacks));
    }

    protected final Events.Callbacks field(Token token) {
        return callbacks.field(token.literalString());
    }

    protected final Events.Callbacks objectStarted() {
        return callbacks.objectStarted();
    }

    protected final Events.Callbacks arrayStarted() {
        return callbacks.arrayStarted();
    }

    protected final Events.Callbacks string(Token token) {
        return callbacks.string(token.literalString());
    }

    protected final Events.Callbacks truth(Token token) {
        return callbacks.truth(token.literalTruth());
    }

    protected final Events.Callbacks number(Token token) {
        return callbacks.number(token.literalNumber());
    }

    protected final Events.Callbacks nil() {
        return callbacks.nil();
    }

    protected final <T> T fail(String msg, Token actual, TokenType... expected) {
        throw new IllegalStateException(this + " failed: " + msg, new ParseException(actual, expected));
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + (scope == null ? "" : "->" + scope) + "]";
    }
}
