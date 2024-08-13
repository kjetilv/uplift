package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.TokenType;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

abstract sealed class AbstractEventHandler implements EventHandler
    permits ArrayEventHandler, ObjectEventHandler, ValueEventHandler {

    private final AbstractEventHandler parentScope;

    private final Callbacks callbacks;

    AbstractEventHandler(AbstractEventHandler parentScope, Callbacks callbacks) {
        this.parentScope = parentScope;
        this.callbacks = requireNonNull(callbacks, "callbacks");
    }

    @Override
    public final Callbacks callbacks() {
        return callbacks;
    }

    protected abstract AbstractEventHandler with(Callbacks callbacks);

    protected final AbstractEventHandler exit() {
        return parentScope == null ? this : parentScope.with(callbacks);
    }

    protected final AbstractEventHandler exit(Function<Callbacks, Callbacks> action) {
        return parentScope == null ? this : parentScope.with(requireNonNull(action, "action").apply(callbacks));
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
        return callbacks.bool(token.literalTruth());
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
        return getClass().getSimpleName() + "[" + (parentScope == null ? "" : "->" + parentScope) + "]";
    }
}
