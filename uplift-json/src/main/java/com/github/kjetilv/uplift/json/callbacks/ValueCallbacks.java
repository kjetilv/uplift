package com.github.kjetilv.uplift.json.callbacks;

import module java.base;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

public record ValueCallbacks(Consumer<Object> onDone) implements Callbacks {

    @Override
    public Callbacks objectStarted() {
        return new MapCallbacks(this, onDone::accept);
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListCallbacks(this, onDone::accept);
    }

    @Override
    public Callbacks string(Token.Str str) {
        onDone.accept(str.value());
        return this;
    }

    @Override
    public Callbacks bool(boolean bool) {
        onDone.accept(bool);
        return this;
    }

    @Override
    public Callbacks nuul() {
        onDone.accept(null);
        return this;
    }

    @Override
    public Callbacks number(Token.Number number) {
        onDone.accept(number.number());
        return this;
    }

    @Override
    public Callbacks objectEnded() {
        throw new IllegalStateException(this + " cannot end an object");
    }
}
