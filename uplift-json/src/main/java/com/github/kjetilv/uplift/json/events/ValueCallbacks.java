package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.ListCallbacks;
import com.github.kjetilv.uplift.json.MapCallbacks;

import java.util.function.Consumer;

public final class ValueCallbacks implements Callbacks {

    private final Consumer<Object> onDone;

    public ValueCallbacks(Consumer<Object> onDone) {
        this.onDone = onDone;
    }

    @Override
    public Callbacks objectStarted() {
        return new MapCallbacks(this, onDone::accept);
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListCallbacks(this, onDone::accept);
    }

    @Override
    public Callbacks string(String string) {
        onDone.accept(string);
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
    public <N extends Number> Callbacks number(N number) {
        onDone.accept(number);
        return this;
    }

    @Override
    public Callbacks objectEnded() {
        throw new IllegalStateException(this + " cannot end an object");
    }
}
