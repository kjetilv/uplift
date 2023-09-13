package com.github.kjetilv.uplift.json;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.kjetilv.uplift.json.TokenType.BEGIN_ARRAY;
import static com.github.kjetilv.uplift.json.TokenType.BEGIN_OBJECT;
import static com.github.kjetilv.uplift.json.TokenType.BOOL;
import static com.github.kjetilv.uplift.json.TokenType.NUMBER;
import static com.github.kjetilv.uplift.json.TokenType.STRING;

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

    private final EventHandler surroundingScope;

    private final Handler[] handlers;

    public EventHandler(EventHandler surroundingScope, Handler... handlers) {
        this.surroundingScope = surroundingScope;
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

    protected final EventHandler surroundingScope() {
        return surroundingScope;
    }

    protected final EventHandler emit(Consumer<Handler> action) {
        for (Handler handler: handlers) {
            action.accept(handler);
        }
        return this;
    }

    protected final EventHandler value(Token token) {
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

    protected final EventHandler newValue() {
        return new ValueEventHandler(this, handlers());
    }

    protected final <T> T fail(Token actual, TokenType... expected) {
        throw new IllegalStateException(this + " failed", new ParseException(actual, expected));
    }

    protected final EventHandler string(Token token) {
        return emit(handler -> handler.string(token.literalString()));
    }

    private EventHandler failValue(Token token) {
        return fail(token, BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER);
    }

    private EventHandler object(EventHandler surroundingScope) {
        return new ObjectEventHandler(surroundingScope, handlers());
    }

    private ArrayEventHandler array(EventHandler surroundingScope) {
        return new ArrayEventHandler(surroundingScope, handlers());
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
        return getClass().getSimpleName() + "[" + (surroundingScope == null ? "" : "->" + surroundingScope) + "]";
    }
}
