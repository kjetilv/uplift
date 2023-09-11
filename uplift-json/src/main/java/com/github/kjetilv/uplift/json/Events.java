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

    public interface Path {

        Path push(String name);
    }

    public interface Handler {

        void objectStarted();

        void field(String name);

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

    protected final Events handler(String path, BiFunction<Events, Token, Events> handler) {
        return new SimpleEvents(
            surroundingScope(),
            handler,
            handlers()
        );
    }

    protected final Handler[] handlers() {
        return handlers;
    }

    protected final Events surroundingScope() {
        return surroundingScope;
    }

    protected final Events processInSurroundingScope(Token processable) {
        return surroundingScope.apply(processable);
    }

    protected final Events emit(Consumer<Handler> action) {
        for (Handler handler: handlers) {
            action.accept(handler);
        }
        return this;
    }

    protected Events value(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> new ObjectEvents(this, handlers());
            case BEGIN_ARRAY -> new ArrayEvents(this, handlers());
            case STRING -> emit(handler -> handler.string(token.literalString()))
                .surroundingScope();
            case BOOL -> emit(handler -> handler.truth(token.literalTruth()))
                .surroundingScope();
            case NUMBER -> emit(handler -> handler.number(token.literalNumber()))
                .surroundingScope();
            case NIL -> emit(Handler::nil)
                .surroundingScope();
            case COMMA, COLON, END_OBJECT, END_ARRAY -> fail(token, BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER);
        };
    }

    protected Events flatValue(Token token) {
        return switch (token.type()) {
            case BEGIN_OBJECT -> new ObjectEvents(this, handlers());
            case BEGIN_ARRAY -> new ArrayEvents(this, handlers());
            case STRING -> emit(handler -> handler.string(token.literalString()));
            case BOOL -> emit(handler -> handler.truth(token.literalTruth()));
            case NUMBER -> emit(handler -> handler.number(token.literalNumber()));
            case NIL -> emit(Handler::nil);
            case COMMA, COLON, END_OBJECT, END_ARRAY -> fail(token, BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER);
        };
    }

    protected <T> T fail(Token actual, TokenType... expected) {
        throw new IllegalStateException(this + " failed", new ParseException(actual, expected));
    }

    private static final class SimpleEvents extends Events {

        private final BiFunction<Events, Token, Events> action;

        private SimpleEvents(
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
