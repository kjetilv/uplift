package com.github.kjetilv.uplift.json.callbacks;

import module java.base;
import com.github.kjetilv.uplift.json.Callbacks;

public record NullCallbacks(Callbacks parent) implements Callbacks {

    @Override
    public Callbacks objectEnded() {
        return parent;
    }
}
