package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.HashKind;

/// Canonicalizes [hashed trees][HashedTree] to [canonical values][CanonicalValue].
///
/// @param <K> Type of Map keys
/// @param <H> Hash kind
@FunctionalInterface
public interface Canonicalizer<K, H extends HashKind<H>> {

    CanonicalValue<H> canonical(HashedTree<K, H> hashedTree);
}
