package com.github.kjetilv.uplift.json.gens;

import com.github.kjetilv.uplift.json.Events;

public abstract class AppCallbacks implements Events.Callbacks<AppCallbacks> {

    private String currentField;

    @Override
    public AppCallbacks field(String name) {
        currentField = name;
        return this;
    }

    protected String currentField() {
        return currentField;
    }

    protected <T> T fail() {
        throw new IllegalStateException("Unexpected object value for " + currentField());
    }

    protected <T> T fail(Object value) {
        throw new IllegalStateException("Unexpected value for " + currentField() + ": " + value);
    }
}
