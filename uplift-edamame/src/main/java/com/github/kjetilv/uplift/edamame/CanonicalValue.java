package com.github.kjetilv.uplift.edamame;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

/// A canonical value is the result of resolving a  nested [map][Map] against shared
/// substructures of other nested maps, including [hash collisions][Collision].
public sealed interface CanonicalValue<K extends HashKind<K>> {

    /// @return Canonical value
    Object value();

    /// @return Hash of the canonical value
    Hash<K> hash();

    record Node<MK, K extends HashKind<K>>(Hash<K> hash, Map<MK, Object> value) implements CanonicalValue<K> {
        public Node(Hash<K> hash, Map<MK, Object> value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Nodes<K extends HashKind<K>>(Hash<K> hash, List<?> value) implements CanonicalValue<K> {
        public Nodes(Hash<K> hash, List<?> value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Leaf<K extends HashKind<K>>(Hash<K> hash, Object value) implements CanonicalValue<K> {
        public Leaf(Hash<K> hash, Object value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Collision<K extends HashKind<K>>(Hash<K> hash, Object value) implements CanonicalValue<K> {
        public Collision(Hash<K> hash, Object value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Null<K extends HashKind<K>>(HashKind<K> kind) implements CanonicalValue<K> {

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

        public Hash<K> hash() {
            return kind.blank();
        }

        private static final Null<HashKind.K128> NULL_K128 = new Null<>(HashKind.K128);

        private static final Null<HashKind.K128> NULL_K256 = new Null<>(HashKind.K128);
    }
}
