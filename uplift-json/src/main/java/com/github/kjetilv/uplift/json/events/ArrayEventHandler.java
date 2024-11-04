package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Token;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

final class ArrayEventHandler extends AbstractEventHandler {

    ArrayEventHandler(AbstractEventHandler parent, Callbacks callbacks) {
        super(parent, callbacks);
    }

    @Override
    public EventHandler handle(Token token) {
        return switch (token.type()) {
            case BEGIN_ARRAY -> new ArrayEventHandler(new PostValueHandler(this, callbacks()), arrayStarted());
            case BEGIN_OBJECT -> new ObjectEventHandler(new PostValueHandler(this, callbacks()), objectStarted());
            case STRING -> new PostValueHandler(this, string(token));
            case BOOL -> new PostValueHandler(this,truth(token));
            case NUMBER -> new PostValueHandler(this, number(token));
            case NULL -> new PostValueHandler(this, null_());
            case END_ARRAY -> exitScope(Callbacks::arrayEnded);
            default -> fail(
                "Malformed array",
                token,
                BEGIN_OBJECT, BEGIN_ARRAY, STRING, BOOL, NUMBER, NULL
            );
        };
    }

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
        return new ArrayEventHandler(exitScope(), callbacks);
    }

    static final class PostValueHandler extends AbstractEventHandler {

        public PostValueHandler(AbstractEventHandler parentScope, Callbacks callbacks) {
            super(parentScope, callbacks);
        }

        @Override
        public EventHandler handle(Token token) {
            return switch (token.type()) {
                case END_ARRAY -> exitScope().exitScope(Callbacks::arrayEnded);
                case COMMA -> exitScope();
                default -> fail("Malformed array", token, END_ARRAY, COMMA);
            };
        }

        @Override
        protected AbstractEventHandler with(Callbacks callbacks) {
            return new PostValueHandler(exitScope(), callbacks());
        }
    }
}
