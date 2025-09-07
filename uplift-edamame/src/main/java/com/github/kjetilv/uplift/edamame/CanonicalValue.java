package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/// A canonical value is the result of resolving a  nested [map][Map] against shared
/// substructures of other nested maps, including [hash collisions][Collision].
public sealed interface CanonicalValue<H extends HashKind<H>> {

    /// @return The canonical value
    Object value();

    /// @return The hash of the canonical value
    Hash<H> hash();

    record Node<K, H extends HashKind<H>>(Hash<H> hash, Map<K, Object> value) implements CanonicalValue<H> {

        public Node(Hash<H> hash, Map<K, Object> value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Nodes<H extends HashKind<H>>(Hash<H> hash, List<?> value) implements CanonicalValue<H> {

        public Nodes(Hash<H> hash, List<?> value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Leaf<H extends HashKind<H>>(Hash<H> hash, Object value) implements CanonicalValue<H> {

        public Leaf(Hash<H> hash, Object value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Collision<H extends HashKind<H>>(Hash<H> hash, Object value) implements CanonicalValue<H> {

        public static <H extends HashKind<H>> CanonicalValue<H> create(Hash<H> hash, Object object) {
            return new Collision<>(hash, object);
        }

        public Collision(Hash<H> hash, Object value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Null<H extends HashKind<H>>(Hash<H> hash) implements CanonicalValue<H> {

        @Override
        public Object value() {
            return null;
        }
    }
}
