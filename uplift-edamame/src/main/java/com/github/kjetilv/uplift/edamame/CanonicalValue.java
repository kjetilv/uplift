package com.github.kjetilv.uplift.edamame;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

/// A canonical value is the result of resolving a  nested [map][Map] against shared
/// substructures of other nested maps, including [hash collisions][Collision].
public sealed interface CanonicalValue<H extends HashKind<H>> {

    /// @return Canonical value
    Object value();

    /// @return Hash of the canonical value
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
        public Collision(Hash<H> hash, Object value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Null<H extends HashKind<H>>(HashKind<H> kind) implements CanonicalValue<H> {

        @SuppressWarnings("unchecked")
        public static <H extends HashKind<H>> CanonicalValue.Null<H> instanceFor(HashKind<H> kind) {
            return (Null<H>) switch (kind) {
                case HashKind.K256 _ -> NULL_K256;
                case HashKind.K128 _ -> NULL_K128;
            };
        }

        @Override
        public Object value() {
            return null;
        }

        @Override
        public Hash<H> hash() {
            return kind.blank();
        }

        private static final Null<HashKind.K128> NULL_K128 = new Null<>(HashKind.K128);

        private static final Null<HashKind.K128> NULL_K256 = new Null<>(HashKind.K128);
    }
}
