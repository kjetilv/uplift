package com.github.kjetilv.uplift.json;

import module java.base;

public interface JsonSession {

    Callbacks callbacks(Consumer<Object> onDone);
}
