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
            case NUMBER -> new PostValueHandler(this, number(token));
            case STRING -> new PostValueHandler(this, string(token));
            case BOOL -> new PostValueHandler(this, truth(token));
            case BEGIN_ARRAY -> new ArrayEventHandler(new PostValueHandler(this), arrayStarted());
            case BEGIN_OBJECT -> new ObjectEventHandler(new PostValueHandler(this), objectStarted());
            case NULL -> new PostValueHandler(this, null_());
            case END_ARRAY -> exitScope(Callbacks::arrayEnded);
            default -> fail(
                "Malformed array",
                token,
                NUMBER, STRING, BOOL, BEGIN_ARRAY, BEGIN_OBJECT, NULL, END_ARRAY
                );
        };
    }

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
        return new ArrayEventHandler(parentScope(), callbacks);
    }

    static final class PostValueHandler extends AbstractEventHandler {

        public PostValueHandler(AbstractEventHandler parentScope) {
            this(parentScope, null);
        }

        public PostValueHandler(AbstractEventHandler parentScope, Callbacks callbacks) {
            super(parentScope, callbacks);
        }

        @Override
        public EventHandler handle(Token token) {
            return switch (token.type()) {
                case END_ARRAY -> exitScope().exitScope(Callbacks::arrayEnded);
                case COMMA -> exitScope();
                default -> fail("Malformed object level", token, END_ARRAY, COMMA);
            };
        }

        @Override
        protected AbstractEventHandler with(Callbacks callbacks) {
            return new PostValueHandler(parentScope(), callbacks);
        }
    }
}
