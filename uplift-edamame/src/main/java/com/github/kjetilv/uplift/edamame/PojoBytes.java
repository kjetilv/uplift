package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.MapMemoizerFactory;

/**
 * Say you have a Java object.  How do you turn it into a byte array for hashing?  This interface knows how.
 * Implement it and pass it {@link MapsMemoizers#create(KeyHandler, PojoBytes, com.github.kjetilv.uplift.hash.HashKind) here} or
 * {@link MapsMemoizers#create(PojoBytes, com.github.kjetilv.uplift.hash.HashKind) here}.
 */
@SuppressWarnings("unused")
public interface PojoBytes {

    /**
     * Uses hashcode to derive four bytes. Used by default.
     */
    PojoBytes HASHCODE = MapMemoizerFactory.HASHCODE;

    /**
     * Uses {@link Object#toString()} to derive bytes from the string
     */
    PojoBytes TOSTRING = MapMemoizerFactory.TOSTRING;

    /**
     * @return If true, takes on the responsibility for all values
     */
    default boolean overrideDefaults() {
        return false;
    }

    byte[] bytes(Object pojo);
}
