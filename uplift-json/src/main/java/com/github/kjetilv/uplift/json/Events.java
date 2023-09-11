package com.github.kjetilv.uplift.json;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.kjetilv.uplift.json.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

public abstract class Events implements Function<Token, Events> {

    public interface Handler {

        void objectStarted();

        void objectEnded();

        void arrayStarted();

        void arrayEnded();

        void truth(boolean truth);

        void number(Number number);

        void nil();

        void string(String string);
    }

    private final Events surroundingScope;

    private final Handler[] handlers;

    public Events(Events surroundingScope, Handler... handlers) {
        this.surroundingScope = surroundingScope;
        this.handlers = handlers;

        if (this.handlers.length == 0) {
            throw new IllegalArgumentException(this + " has no handlers");
        }
    }

    @Override
    public final Events apply(Token token) {
        return process(token);
    }

    public abstract Events process(Token token);

    protected final Events handler(BiFunction<Events, Token, Events> handler) {
        return new DelegatingEvents(surroundingScope(), handler, handlers());
    }

    protected final Handler[] handlers() {
        return handlers;
    }

    protected final Events surroundingScope() {
        return surroundingScope;
    }

    protected final Events emit(Consumer<Handler> action) {
        for (Handler handler: handlers) {
            action.accept(handler);
        }
        return this;
    }

    protected final Events value(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> object(surroundingScope());
            case BEGIN_ARRAY -> array(surroundingScope());
            case STRING -> string(token).surroundingScope();
            case BOOL -> truth(token).surroundingScope();
            case NUMBER -> number(token).surroundingScope();
            case NIL -> nil().surroundingScope();
            case COMMA, COLON, END_OBJECT, END_ARRAY -> failValue(token);
        };
    }

    protected final Events flatValue(Token token) {
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

    protected final Events newValue() {
        return new ValueEvents(this, handlers());
    }

    protected final <T> T fail(Token actual, TokenType... expected) {
        throw new IllegalStateException(this + " failed", new ParseException(actual, expected));
    }

    protected final Events string(Token token) {
        return emit(handler -> handler.string(token.literalString()));
    }

    private Events failValue(Token token) {
        return fail(token, BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER);
    }

    private ObjectEvents object(Events surroundingScope) {
        return new ObjectEvents(surroundingScope, handlers());
    }

    private ArrayEvents array(Events surroundingScope) {
        return new ArrayEvents(surroundingScope, handlers());
    }

    private Events number(Token token) {
        return emit(handler -> handler.number(token.literalNumber()));
    }

    private Events truth(Token token) {
        return emit(handler -> handler.truth(token.literalTruth()));
    }

    private Events nil() {
        return emit(Handler::nil);
    }

    private static final class DelegatingEvents extends Events {

        private final BiFunction<Events, Token, Events> action;

        private DelegatingEvents(
            Events surroundingScope,
            BiFunction<Events, Token, Events> action,
            Handler... handlers
        ) {
            super(surroundingScope, handlers);
            this.action = action;
        }

        @Override
        public Events process(Token token) {
            return action.apply(this, token);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + (surroundingScope == null ? "" : "->" + surroundingScope) + "]";
    }
}
