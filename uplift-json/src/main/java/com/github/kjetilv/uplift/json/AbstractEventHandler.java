package com.github.kjetilv.uplift.json;

import java.util.Arrays;
import java.util.function.Function;

import static com.github.kjetilv.uplift.json.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

abstract class AbstractEventHandler implements EventHandler {

    private final EventHandler scope;

    private final Callbacks[] callbacks;

    AbstractEventHandler(EventHandler scope, Callbacks... callbacks) {
        this.scope = scope;
        this.callbacks = callbacks;
        if (this.callbacks.length == 0) {
            throw new IllegalArgumentException(this + " has no handlers");
        }
    }

    @Override
    public final EventHandler apply(Token token) {
        return process(token);
    }

    protected abstract AbstractEventHandler withCallbacks(Callbacks... callbacks);

    protected final EventHandler scope() {
        return scope;
    }

    protected final AbstractEventHandler emit(Function<Callbacks, Callbacks> action) {
        return withCallbacks(
            Arrays.stream(callbacks).map(action).toArray(Callbacks[]::new)
        );
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

    protected final void startObject() {
        emit(Callbacks::objectStarted);
    }

    protected EventHandler endObject() {
        return emit(Callbacks::objectEnded).scope();
    }

    protected final void startArray() {
        emit(Callbacks::arrayStarted);
    }

    protected EventHandler endArray() {
        return emit(Callbacks::arrayEnded).scope();
    }

    protected final void field(Token token) {
        emit(handler -> handler.field(token.literalString()));
    }

    protected final AbstractEventHandler string(Token token) {
        return emit(handler -> handler.string(token.literalString()));
    }

    protected ValueEventHandler value() {
        return new ValueEventHandler(this, callbacks);
    }

    private EventHandler failValue(Token token) {
        return fail(token, BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER);
    }

    private EventHandler object(EventHandler scope) {
        return new ObjectEventHandler(scope, callbacks);
    }

    private ArrayEventHandler array(EventHandler scope) {
        return new ArrayEventHandler(scope, callbacks);
    }

    private AbstractEventHandler number(Token token) {
        return emit(handler -> handler.number(token.literalNumber()));
    }

    private AbstractEventHandler truth(Token token) {
        return emit(handler -> handler.truth(token.literalTruth()));
    }

    private AbstractEventHandler nil() {
        return emit(Callbacks::nil);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + (scope == null ? "" : "->" + scope) + "]";
    }
}
