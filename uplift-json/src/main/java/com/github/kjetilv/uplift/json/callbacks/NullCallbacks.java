package com.github.kjetilv.uplift.json.callbacks;

import module java.base;
import module uplift.json;

public record NullCallbacks(Callbacks parent) implements Callbacks {

    @Override
    public Callbacks objectEnded() {
        return parent;
    }
}
