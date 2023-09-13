package com.github.kjetilv.uplift.json;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.kjetilv.uplift.json.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

public abstract sealed class EventHandler implements Function<Token, EventHandler>
    permits ValueEventHandler, ArrayEventHandler, ObjectEventHandler, DelegatedEventHandler {

    public static EventHandler create(Callbacks... callbacks) {
        return new ValueEventHandler(callbacks);
    }

    public interface Callbacks {

        void objectStarted();

        void objectEnded();

        void arrayStarted();

        void arrayEnded();

        void truth(boolean truth);

        void number(Number number);

        void nil();

        void string(String string);
    }

    private final EventHandler scope;

    private final Callbacks[] callbacks;

    EventHandler(EventHandler scope, Callbacks... callbacks) {
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

    public abstract EventHandler process(Token token);

    protected final Callbacks[] callbacks() {
        return callbacks;
    }

    protected final EventHandler scope() {
        return scope;
    }

    protected final EventHandler emit(Consumer<Callbacks> action) {
        for (Callbacks callbacks: this.callbacks) {
            action.accept(callbacks);
        }
        return this;
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

    protected final EventHandler string(Token token) {
        return emit(handler -> handler.string(token.literalString()));
    }

    protected final EventHandler eventHandler(Function<Token, EventHandler> eventHandler) {
        return new DelegatedEventHandler(scope(), eventHandler, callbacks);
    }

    private final EventHandler failValue(Token token) {
        return fail(token, BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER);
    }

    private EventHandler object(EventHandler scope) {
        return new ObjectEventHandler(scope, callbacks());
    }

    private ArrayEventHandler array(EventHandler scope) {
        return new ArrayEventHandler(scope, callbacks());
    }

    private EventHandler number(Token token) {
        return emit(handler -> handler.number(token.literalNumber()));
    }

    private EventHandler truth(Token token) {
        return emit(handler -> handler.truth(token.literalTruth()));
    }

    private EventHandler nil() {
        return emit(Callbacks::nil);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + (scope == null ? "" : "->" + scope) + "]";
    }
}
