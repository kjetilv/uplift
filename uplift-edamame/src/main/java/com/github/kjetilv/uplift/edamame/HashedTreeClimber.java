package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.HashKind;

@SuppressWarnings("unused")
public interface HashedTreeClimber<K, H extends HashKind<H>> {

    default HashedTreeClimber<K, H> startMap() {
        return this;
    }

    default HashedTreeClimber<K, H> field(Object key) {
        return this;
    }

    default HashedTreeClimber<K, H> endMap() {
        return this;
    }

    default HashedTreeClimber<K, H> startList() {
        return this;
    }

    default HashedTreeClimber<K, H> endList() {
        return this;
    }

    default HashedTreeClimber<K, H> leaf(Object value) {
        return this;
    }
}
