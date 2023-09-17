package com.github.kjetilv.uplift.json.events;

import java.util.function.Function;

import com.github.kjetilv.uplift.json.Events;
import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.TokenType;

abstract class AbstractEventHandler<C extends Events.Callbacks<C>> implements EventHandler<C> {

    private final AbstractEventHandler<C> scope;

    private final C callbacks;

    AbstractEventHandler(AbstractEventHandler<C> scope, C callbacks) {
        this.scope = scope;
        this.callbacks = callbacks;
    }

    @Override
    public final C callbacks() {
        return callbacks;
    }

    protected abstract AbstractEventHandler<C> with(C callbacks);

    protected final AbstractEventHandler<C> exit() {
        return scope == null ? this : scope.with(callbacks);
    }

    protected final AbstractEventHandler<C> exit(Function<C, C> action) {
        return scope == null ? this : scope.with(action.apply(callbacks));
    }

    protected final C field(Token token) {
        return callbacks.field(token.literalString());
    }

    protected final C objectStarted() {
        return callbacks.objectStarted();
    }

    protected final C arrayStarted() {
        return callbacks.arrayStarted();
    }

    protected final C string(Token token) {
        return callbacks.string(token.literalString());
    }

    protected final C truth(Token token) {
        return callbacks.truth(token.literalTruth());
    }

    protected final C number(Token token) {
        return callbacks.number(token.literalNumber());
    }

    protected final C nil() {
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
