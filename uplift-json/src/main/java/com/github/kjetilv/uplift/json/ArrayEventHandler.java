package com.github.kjetilv.uplift.json;

final class ArrayEventHandler extends AbstractEventHandler {

    ArrayEventHandler(AbstractEventHandler scope, Callbacks callbacks) {
        super(scope, callbacks);
    }

    @Override
    protected AbstractEventHandler with(Callbacks callbacks) {
        return new ArrayEventHandler(scope(), callbacks);
    }

    @Override
    public EventHandler process(Token token) {
        return switch (token.type()) {
            case END_ARRAY -> close(Callbacks::arrayEnded);
            case COMMA -> this;
            default -> flatValue(token);
        };
    }
}
