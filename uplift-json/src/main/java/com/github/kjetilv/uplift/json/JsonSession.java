package com.github.kjetilv.uplift.json;

import java.util.function.Consumer;

public interface JsonSession {

    Callbacks callbacks(Consumer<Object> onDone);
}
