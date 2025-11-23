package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.util.Bytes;

/// Say you have a Java object.  How do you turn it into a byte array for hashing?  This interface knows how.
///
/// Implement it or use [one][#HASHCODE] [of][#TOSTRING] [the][#UNSUPPORTED] presets.
@SuppressWarnings("unused")
public interface PojoBytes {

    /// Uses hashcode to derive four bytes.
    PojoBytes HASHCODE = value -> Bytes.intBytes(value.hashCode());

    /// Uses [Object#toString()] to derive bytes from the string
    PojoBytes TOSTRING = value -> value.toString().getBytes();

    /// Fails if called.  Use when we don't expect any POJOs.  Default value.
    PojoBytes UNSUPPORTED = value -> {
        throw new IllegalArgumentException(
            "Unexpected pojo:" + value + (value == null ? "" : " of " + value.getClass())
        );
    };

    /// @param pojo Plain old Java object
    /// @return Bytes for the object
    byte[] bytes(Object pojo);
}
