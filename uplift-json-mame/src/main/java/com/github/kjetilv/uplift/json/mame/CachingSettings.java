package com.github.kjetilv.uplift.json.mame;

public record CachingSettings(
    boolean preserveNulls,
    boolean collisionsNeverHappen
) {

    boolean collisionsHappen() {
        return !collisionsNeverHappen;
    }
}
