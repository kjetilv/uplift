package com.github.kjetilv.uplift.json;

interface PullParser {

    Callbacks pull(Tokens tokens, Callbacks callbacks);
}
