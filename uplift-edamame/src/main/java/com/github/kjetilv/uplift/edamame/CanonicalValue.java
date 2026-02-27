package com.github.kjetilv.uplift.edamame;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;

/// A canonical value is the result of resolving a  nested [map][Map] against shared
/// substructures of other nested maps, including [hash collisions][Collision].
public sealed interface CanonicalValue<MK, K extends HashKind<K>> {

    /// @return Canonical value
    Object value();

    /// @return Hash of the canonical value
    Hash<K> hash();

    record Node<MK, K extends HashKind<K>>(Hash<K> hash, Map<MK, Object> value)
        implements CanonicalValue<MK, K> {
        public Node(Hash<K> hash, Map<MK, Object> value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Nodes<MK, K extends HashKind<K>>(Hash<K> hash, List<?> value)
        implements CanonicalValue<MK, K> {
        public Nodes(Hash<K> hash, List<?> value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Leaf<MK, K extends HashKind<K>>(Hash<K> hash, Object value)
        implements CanonicalValue<MK, K> {
        public Leaf(Hash<K> hash, Object value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    record Collision<MK, K extends HashKind<K>>(Hash<K> hash, Object value)
        implements CanonicalValue<MK, K> {
        public Collision(Hash<K> hash, Object value) {
            this.hash = Objects.requireNonNull(hash, "hash");
            this.value = Objects.requireNonNull(value, "value");
        }
    }

    @SuppressWarnings("unchecked")
    record Null<MK, K extends HashKind<K>>(HashKind<K> kind) implements CanonicalValue<MK, K> {

        public static <MK, H extends HashKind<H>> CanonicalValue.Null<MK, H> instanceFor(HashKind<H> kind) {
            return (Null<MK, H>) switch (kind) {
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

        private static final Null<?, HashKind.K128> NULL_K128 = new Null<>(HashKind.K128);

        private static final Null<?, HashKind.K128> NULL_K256 = new Null<>(HashKind.K128);
    }
}
