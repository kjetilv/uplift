package com.github.kjetilv.uplift.json.callbacks;

import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.JsonSession;

import java.util.function.Consumer;

public class DefaultJsonSession implements JsonSession {

    @Override
    public Callbacks callbacks(Consumer<Object> onDone) {
        return new ValueCallbacks(onDone);
    }
}
