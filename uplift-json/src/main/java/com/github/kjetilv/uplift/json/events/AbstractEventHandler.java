package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.ParseException;
import com.github.kjetilv.uplift.json.tokens.Token;
import com.github.kjetilv.uplift.json.tokens.TokenType;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

abstract sealed class AbstractEventHandler implements EventHandler permits
    ArrayEventHandler, ArrayEventHandler.PostValueHandler,
    ObjectEventHandler, ObjectEventHandler.PostValueHandler,
    ValueEventHandler {

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

    protected final AbstractEventHandler exitScope() {
        return parentScope == null ? this : parentScope.with(callbacks);
    }

    protected final AbstractEventHandler exitScope(Function<Callbacks, Callbacks> action) {
        return parentScope == null ? this : parentScope.with(action.apply(callbacks));
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

    protected final Callbacks null_() {
        return callbacks.null_();
    }

    @SuppressWarnings("SameParameterValue")
    protected EventHandler skipRequired(
        Token preceding,
        TokenType expected,
        Supplier<EventHandler> then
    ) {
        return skipToken -> {
            if (skipToken.type() == expected) {
                return then.get();
            }
            return fail(
                "Expected " + expected + " to follow field `" + preceding.literalString() + "`",
                skipToken,
                expected
            );
        };
    }

    protected final <T> T fail(String msg, Token actual, TokenType... expected) {
        throw new ParseException(this, actual, expected);
    }

    protected AbstractEventHandler with(Callbacks callbacks) {
        throw new UnsupportedOperationException(this + ": No neesting: " + callbacks);
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + (parentScope == null ? "" : "->" + parentScope) + "]";
    }
}
