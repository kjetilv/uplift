package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.HashKind;

/// Canonicalizes [hashed trees][HashedTree] to [canonical values][CanonicalValue].
///
/// @param <MK> Type of Map keys
/// @param <K> Hash kind
@FunctionalInterface
public interface Canonicalizer<MK, K extends HashKind<K>> {

    CanonicalValue<MK, K> canonical(HashedTree<MK, K> hashedTree);
}
