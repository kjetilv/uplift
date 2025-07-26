package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;

import java.util.function.Consumer;

public interface JsonSession<H extends HashKind<H>> {

    Callbacks onDone(Consumer<Object> onDone);
}
