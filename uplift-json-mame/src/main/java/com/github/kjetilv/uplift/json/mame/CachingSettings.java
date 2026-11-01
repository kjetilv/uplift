package com.github.kjetilv.uplift.json.mame;

/// @param preserveNulls         If true, null values are kept in maps and lists. If false, they are left out.
/// @param collisionsNeverHappen If true, values are not checked for hash collisions. This is faster, but if two
///                              different values do share a hash, the second is silently replaced by the first.
///                              Only use this with [K256][com.github.kjetilv.uplift.hash.HashKind.K256] hashes,
///                              or with trusted input. [K128][com.github.kjetilv.uplift.hash.HashKind.K128] uses
///                              MD5, for which colliding inputs can be constructed on purpose.
public record CachingSettings(
    boolean preserveNulls,
    boolean collisionsNeverHappen
) {

    boolean collisionsHappen() {
        return !collisionsNeverHappen;
    }
}
