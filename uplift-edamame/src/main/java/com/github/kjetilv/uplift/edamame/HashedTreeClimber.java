package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.HashKind;

public interface HashedTreeClimber<K, H extends HashKind<H>> {

    default HashedTreeClimber<K, H> startMap() {
        throw new UnsupportedOperationException(this.toString());
    }

    default HashedTreeClimber<K, H> field(Object key) {
        throw new UnsupportedOperationException(this.toString());
    }

    default HashedTreeClimber<K, H> endMap() {
        throw new UnsupportedOperationException(this.toString());
    }

    default HashedTreeClimber<K, H> startList() {
        throw new UnsupportedOperationException(this.toString());
    }

    default HashedTreeClimber<K, H> endList() {
        throw new UnsupportedOperationException(this.toString());
    }

    default HashedTreeClimber<K, H> value(Object value) {
        throw new UnsupportedOperationException(this.toString());
    }

    default HashedTree<K, H> climb() {
        throw new UnsupportedOperationException(this.toString());
    }
}
