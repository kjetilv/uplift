package com.github.kjetilv.uplift.json;

import java.util.function.Function;

abstract class AbstractEventHandler implements EventHandler {

    private final AbstractEventHandler scope;

    private final Callbacks callbacks;

    AbstractEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        this.scope = scope;
        this.callbacks = callbacks;
    }

    @Override
    public final EventHandler apply(Token token) {
        return process(token);
    }

    final Callbacks getCallbacks() {
        return callbacks;
    }

    protected abstract AbstractEventHandler with(Callbacks callbacks);

    protected final AbstractEventHandler exit() {
        return scope == null ? this : scope.with(callbacks);
    }

    protected final AbstractEventHandler exit(Function<Callbacks, Callbacks> action) {
        return scope == null ? this : scope.with(action.apply(callbacks));
    }

    protected final Callbacks field(Token token) {
        return callbacks.field(token.literalString());
    }

    protected final Callbacks objectStarted() {
        return callbacks.objectStarted();
    }

    protected final Callbacks arrayStarted() {
        return callbacks.arrayStarted();
    }

    protected final Callbacks string(Token token) {
        return callbacks.string(token.literalString());
    }

    protected final Callbacks truth(Token token) {
        return callbacks.truth(token.literalTruth());
    }

    protected final Callbacks number(Token token) {
        return callbacks.number(token.literalNumber());
    }

    protected final Callbacks nil() {
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
