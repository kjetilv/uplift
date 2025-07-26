package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.json.Callbacks;

import java.util.function.Consumer;

public interface JsonSession {

    Callbacks onDone(Consumer<Object> onDone);
}
