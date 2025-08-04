package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.hash.Hashes;

/**
 * Say you have a Java object.  How do you turn it into a byte array for hashing?  This interface knows how.
 * <p>
 * Implement it or use {@link #HASHCODE one} {@link #TOSTRING of} {@link #UNSUPPORTED the} presets.
 */
@SuppressWarnings("unused")
public interface PojoBytes {

    /**
     * Uses hashcode to derive four bytes.
     */
    PojoBytes HASHCODE = value -> Hashes.bytes(value.hashCode());

    /**
     * Uses {@link Object#toString()} to derive bytes from the string
     */
    PojoBytes TOSTRING = value -> value.toString().getBytes();

    /**
     * Fails if called.  Use when we don't expect any POJOs.  Default value.
     */
    PojoBytes UNSUPPORTED = value -> {
        throw new IllegalArgumentException(
            "Unexpected pojo:" + value + (value == null ? "" : " of " + value.getClass())
        );
    };

    byte[] bytes(Object pojo);
}
