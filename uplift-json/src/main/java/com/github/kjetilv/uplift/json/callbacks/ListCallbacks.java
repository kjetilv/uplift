package com.github.kjetilv.uplift.json.callbacks;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ListCallbacks implements Callbacks {

    private final Callbacks parent;

    private final Consumer<List<Object>> onDone;

    private final List<Object> list;

    public ListCallbacks(Callbacks parent, Consumer<List<Object>> onDone) {
        this.parent = parent;
        this.list = new ArrayList<>();
        this.onDone = onDone;
    }

    @Override
    public Callbacks objectStarted() {
        return new MapCallbacks(this, list::add);
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListCallbacks(this, list::add);
    }

    @Override
    public Callbacks arrayEnded() {
        onDone.accept(list);
        return parent;
    }

    @Override
    public Callbacks string(Token.Str str) {
        list.add(str.value());
        return this;
    }

    @Override
    public Callbacks bool(boolean bool) {
        list.add(bool);
        return this;
    }

    @Override
    public Callbacks nuul() {
        list.add(null);
        return this;
    }

    @Override
    public Callbacks number(Token.Number number) {
        list.add(number.number());
        return this;
    }

    @Override
    public Callbacks objectEnded() {
        throw new IllegalStateException(this + " cannot end an object");
    }
}
