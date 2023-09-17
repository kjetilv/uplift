package com.github.kjetilv.uplift.json.events;

import com.github.kjetilv.uplift.json.tokens.Token;

@FunctionalInterface
interface EventHandler {

    EventHandler process(Token token);
}
