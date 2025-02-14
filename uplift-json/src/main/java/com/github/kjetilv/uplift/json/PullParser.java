package com.github.kjetilv.uplift.json;

public interface PullParser {

    Callbacks pull(Tokens tokens, Callbacks callbacks);
}
