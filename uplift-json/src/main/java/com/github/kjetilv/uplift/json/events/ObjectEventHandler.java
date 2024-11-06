package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.tokens.Token;

import java.util.function.Supplier;

import static com.github.kjetilv.uplift.json.tokens.TokenType.*;

final class ObjectEventHandler extends AbstractEventHandler {

    ObjectEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    public EventHandler handle(Token token) {
        return switch (token.type()) {
            case STRING -> skipColon(() ->
                new ValueEventHandler(new PostValueHandler(this), field(token))
            );
            case END_OBJECT -> exitScope(Callbacks::objectEnded);
            default -> fail("Malformed object level", token, END_OBJECT, STRING);
        };
    }

    @Override
    protected ObjectEventHandler with(Callbacks callbacks) {
        return new ObjectEventHandler(exitScope(), callbacks);
    }

    @SuppressWarnings("SameParameterValue")
    private EventHandler skipColon(Supplier<EventHandler> then) {
        return skipToken -> {
            if (skipToken.type() == COLON) {
                return then.get();
            }
            return fail(
                "Expected " + COLON,
                skipToken,
                COLON
            );
        };
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
                case END_OBJECT -> exitScope().exitScope(Callbacks::objectEnded);
                case COMMA -> exitScope();
                default -> fail("Malformed object level", token, END_OBJECT, COMMA);
            };
        }

        @Override
        protected AbstractEventHandler with(Callbacks callbacks) {
            return new PostValueHandler(parentScope(), callbacks);
        }
    }
}
