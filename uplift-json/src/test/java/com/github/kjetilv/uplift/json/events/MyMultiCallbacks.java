package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.Callbacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

final class MyMultiCallbacks implements Callbacks {

    static final AtomicInteger COUNT = new AtomicInteger();

    private final List<List<String>> stuff;

    private final int count;

    private final AtomicBoolean called = new AtomicBoolean();

    private final AtomicReference<Throwable> callStack = new AtomicReference<>();

    MyMultiCallbacks() {
        this(null);
    }

    MyMultiCallbacks(List<List<String>> stuff) {
        this.count = COUNT.getAndIncrement();
        if (stuff == null) {
            this.stuff = new ArrayList<>();
            this.stuff.add(new ArrayList<>());
        } else {
            this.stuff = stuff;
        }
    }

    List<List<String>> getStuff() {
        return stuff;
    }

    @Override
    public MyMultiCallbacks objectStarted() {
        return add("objectStarted");
    }

    @Override
    public boolean multi() {
        return true;
    }

    @Override
    public Callbacks line() {
        stuff.add(new ArrayList<>());
        return new MyMultiCallbacks(stuff);
    }

    @Override
    public MyMultiCallbacks field(String name) {
        return add("field:" + name);
    }

    @Override
    public MyMultiCallbacks objectEnded() {
        return add("objectEnded");
    }

    @Override
    public MyMultiCallbacks arrayStarted() {
        return add("arrayStarted");
    }

    @Override
    public MyMultiCallbacks string(String string) {
        return add("string:" + string);
    }

    @Override
    public MyMultiCallbacks number(Number number) {
        return add("number:" + number);
    }

    @Override
    public MyMultiCallbacks bool(boolean bool) {
        return add("truth:" + bool);
    }

    @Override
    public MyMultiCallbacks null_() {
        return add("nil");
    }

    @Override
    public MyMultiCallbacks arrayEnded() {
        return add("arrayEnded");
    }

    private MyMultiCallbacks add(String event) {
        if (called.compareAndSet(false, true)) {
            stuff.getLast().add(event);
            callStack.set(new Throwable());
            return new MyMultiCallbacks(stuff);
        }
        throw new IllegalStateException("Called again! Count is " + count + ", stuff: " + stuff + ", event: " + event, callStack.get());
    }

    @Override
    public String toString() {
        return stuff.stream().map(part -> "  " + String.join("\n  ", part))
            .collect(Collectors.joining("\n---\n"));
    }
}
