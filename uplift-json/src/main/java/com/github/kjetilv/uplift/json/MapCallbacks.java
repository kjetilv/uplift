package com.github.kjetilv.uplift.json;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class MapCallbacks implements Callbacks {

    public static MapCallbacks create(Callbacks parent, Consumer<Map<String, Object>> onDone) {
        return new MapCallbacks(parent, onDone);
    }

    private final Callbacks parent;

    private final Consumer<Map<String, Object>> onDone;

    private final Map<String, Object> map;

    private String currentField;

    public MapCallbacks(Callbacks parent, Consumer<Map<String, Object>> onDone) {
        this.parent = parent;
        this.map = new LinkedHashMap<>();
        this.onDone = onDone;
    }

    @Override
    public Callbacks objectStarted() {
        return new MapCallbacks(this, this::set);
    }

    @Override
    public Callbacks field(Token.Field token) {
        this.currentField = token.value();
        return this;
    }

    @Override
    public Callbacks string(Token.String string) {
        return set(string.value());
    }

    @Override
    public Callbacks number(Token.Number number) {
        return set(number.number());
    }

    @Override
    public Callbacks bool(boolean bool) {
        return set(bool);
    }

    @Override
    public Callbacks nuul() {
        return set(null);
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListCallbacks(this, this::set);
    }

    @Override
    public Callbacks arrayEnded() {
        throw new IllegalStateException(this + " cannot end an array");
    }

    @Override
    public Callbacks objectEnded() {
        onDone.accept(map);
        return parent;
    }

    private Callbacks set(Object value) {
        this.map.put(currentField, value);
        return this;
    }
}
