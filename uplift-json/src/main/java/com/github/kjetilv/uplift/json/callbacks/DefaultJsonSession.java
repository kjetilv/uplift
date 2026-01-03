package com.github.kjetilv.uplift.json.callbacks;

import module java.base;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonSession;

public final class DefaultJsonSession implements JsonSession {

    @Override
    public Callbacks callbacks(Consumer<Object> onDone) {
        return new ValueCallbacks(onDone);
    }
}
