package com.github.kjetilv.uplift.json;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.kjetilv.uplift.json.TokenType.*;

public abstract class EventHandler implements Function<Token, EventHandler> {

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

    private final EventHandler scope;

    private final Handler[] handlers;

    public EventHandler(EventHandler scope, Handler... handlers) {
        this.scope = scope;
        this.handlers = handlers;

        if (this.handlers.length == 0) {
            throw new IllegalArgumentException(this + " has no handlers");
        }
    }

    @Override
    public final EventHandler apply(Token token) {
        return process(token);
    }

    public abstract EventHandler process(Token token);

    protected final Handler[] handlers() {
        return handlers;
    }

    protected final EventHandler scope() {
        return scope;
    }

    protected final EventHandler emit(Consumer<Handler> action) {
        for (Handler handler: handlers) {
            action.accept(handler);
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

    protected Skip skip(TokenType skipped, ValueEventHandler next) {
        return new Skip(scope(), skipped, next, handlers());
    }

    private EventHandler failValue(Token token) {
        return fail(token, BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER);
    }

    private EventHandler object(EventHandler scope) {
        return new ObjectEventHandler(scope, handlers());
    }

    private ArrayEventHandler array(EventHandler scope) {
        return new ArrayEventHandler(scope, handlers());
    }

    private EventHandler number(Token token) {
        return emit(handler -> handler.number(token.literalNumber()));
    }

    private EventHandler truth(Token token) {
        return emit(handler -> handler.truth(token.literalTruth()));
    }

    private EventHandler nil() {
        return emit(Handler::nil);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + (scope == null ? "" : "->" + scope) + "]";
    }
}
