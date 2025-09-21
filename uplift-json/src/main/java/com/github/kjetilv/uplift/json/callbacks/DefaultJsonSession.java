package com.github.kjetilv.uplift.json.callbacks;

import module java.base;
import module uplift.json;

public class DefaultJsonSession implements JsonSession {

    @Override
    public Callbacks callbacks(Consumer<Object> onDone) {
        return new ValueCallbacks(onDone);
    }
}
