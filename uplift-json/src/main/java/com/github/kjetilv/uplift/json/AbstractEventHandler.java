package com.github.kjetilv.uplift.json;

import java.util.function.Function;

import static com.github.kjetilv.uplift.json.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

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

    Callbacks getCallbacks() {
        return callbacks;
    }

    protected abstract AbstractEventHandler with(Callbacks callbacks);

    protected final AbstractEventHandler scope() {
        return scope == null ? this : scope.with(callbacks);
    }

    protected final EventHandler value(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> object(scope());
            case BEGIN_ARRAY -> array(scope());
            case STRING -> string(token).scope();
            case BOOL -> truth(token).scope();
            case NUMBER -> number(token).scope();
            case NIL -> nil().scope();
            case COMMA, COLON, END_OBJECT, END_ARRAY -> failValue(token);
        };
    }

    protected final EventHandler flatValue(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> object(this);
            case BEGIN_ARRAY -> array(this);
            case STRING -> string(token);
            case BOOL -> truth(token);
            case NUMBER -> number(token);
            case NIL -> nil();
            case COMMA, COLON, END_OBJECT, END_ARRAY -> failValue(token);
        };
    }

    protected final <T> T fail(Token actual, TokenType... expected) {
        throw new IllegalStateException(this + " failed", new ParseException(actual, expected));
    }

    protected final Callbacks field(Token token) {
        return callbacks.field(token.literalString());
    }

    protected final AbstractEventHandler string(Token token) {
        return with(callbacks.string(token.literalString()));
    }

    protected AbstractEventHandler close(Function<Callbacks, Callbacks> action) {
        return scope == null ? this : scope.with(action.apply(callbacks));
    }

    private AbstractEventHandler number(Token token) {
        return with(callbacks.number(token.literalNumber()));
    }

    private AbstractEventHandler truth(Token token) {
        return with(callbacks.truth(token.literalTruth()));
    }

    private EventHandler failValue(Token token) {
        return fail(token, BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER);
    }

    private EventHandler object(AbstractEventHandler scope) {
        return new ObjectEventHandler(scope, callbacks.objectStarted());
    }

    private ArrayEventHandler array(AbstractEventHandler scope) {
        return new ArrayEventHandler(scope, callbacks.arrayStarted());
    }

    private AbstractEventHandler nil() {
        return with(callbacks.nil());
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + (scope == null ? "" : "->" + scope) + "]";
    }
}
