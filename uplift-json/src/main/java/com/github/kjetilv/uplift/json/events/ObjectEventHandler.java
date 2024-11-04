package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Token;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

final class ObjectEventHandler extends AbstractEventHandler {

    ObjectEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    public EventHandler handle(Token token) {
        return switch (token.type()) {
            // A field
            case STRING -> skipRequired(
                token,
                COLON,
                () ->
                    new ValueEventHandler(
                        new PostValueHandler(this, callbacks()),
                        field(token))
            );
            case END_OBJECT -> exitScope(Callbacks::objectEnded);
            default -> fail("Malformed object level", token, END_OBJECT, STRING);
        };
    }

    @Override
    protected ObjectEventHandler with(Callbacks callbacks) {
        return new ObjectEventHandler(exitScope(), callbacks);
    }

    static final class PostValueHandler extends AbstractEventHandler {

        public PostValueHandler(AbstractEventHandler parentScope, Callbacks callbacks) {
            super(parentScope, callbacks);
        }

        @Override
        public EventHandler handle(Token token) {
            return switch (token.type()) {
                case END_OBJECT -> exitScope().exitScope(Callbacks::objectEnded);
                case COMMA -> exitScope();
                default -> fail("Malformed object level", token, END_OBJECT, COMMA);
            };
        }

        @Override
        protected AbstractEventHandler with(Callbacks callbacks) {
            return new PostValueHandler(exitScope(), callbacks);
        }
    }
}
