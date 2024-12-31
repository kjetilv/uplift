package com.github.kjetilv.uplift.json;

public class NullCallbacks implements Callbacks {

    private final Callbacks parent;

    public NullCallbacks(Callbacks parent) {
        this.parent = parent;
    }

    @Override
    public Callbacks objectEnded() {
        return parent;
    }
}
