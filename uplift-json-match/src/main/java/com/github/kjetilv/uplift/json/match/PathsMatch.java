package com.github.kjetilv.uplift.json.match;

import java.util.List;

record PathsMatch<T>(List<? extends Probe<T>> pathways) implements Match<T> {

    @Override
    public boolean matches() {
        return pathways().stream().allMatch(Probe::found);
    }
}
