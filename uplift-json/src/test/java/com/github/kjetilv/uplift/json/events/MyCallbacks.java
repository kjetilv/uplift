package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

final class MyCallbacks implements Callbacks {

    static final AtomicInteger COUNT = new AtomicInteger();

    private final List<String> stuff;

    private final int count;

    private final AtomicBoolean called = new AtomicBoolean();

    private final AtomicReference<Throwable> callStack = new AtomicReference<>();

    MyCallbacks() {
        this(null);
    }

    MyCallbacks(List<String> stuff) {
        this.count = COUNT.getAndIncrement();
        this.stuff = stuff == null ? Collections.emptyList() : List.copyOf(stuff);
    }

    List<String> getStuff() {
        return stuff;
    }

    @Override
    public MyCallbacks objectStarted() {
        return add("objectStarted");
    }

    @Override
    public MyCallbacks field(Token.Field token) {
        return add("field:" + token.value());
    }

    @Override
    public MyCallbacks objectEnded() {
        return add("objectEnded");
    }

    @Override
    public MyCallbacks arrayStarted() {
        return add("arrayStarted");
    }

    @Override
    public MyCallbacks string(Token.String string) {
        return add("string:" + string.value());
    }

    @Override
    public MyCallbacks number(Token.Number number) {
        return add("number:" + number.number());
    }

    @Override
    public MyCallbacks bool(boolean bool) {
        return add("truth:" + bool);
    }

    @Override
    public MyCallbacks nuul() {
        return add("nil");
    }

    @Override
    public MyCallbacks arrayEnded() {
        return add("arrayEnded");
    }

    private MyCallbacks add(String event) {
        if (called.compareAndSet(false, true)) {
            ArrayList<String> moreStuff = new ArrayList<>(stuff);
            moreStuff.add(event);
            callStack.set(new Throwable());
            return new MyCallbacks(moreStuff);
        }
        throw new IllegalStateException("Called again! Count is " + count + ", stuff: " + stuff + ", event: " + event, callStack.get());
    }

    @Override
    public String toString() {
        return java.lang.String.join("\n", stuff);
    }
}
