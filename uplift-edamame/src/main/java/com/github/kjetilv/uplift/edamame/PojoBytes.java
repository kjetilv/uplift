package com.github.kjetilv.uplift.edamame;

import com.github.kjetilv.uplift.edamame.impl.MapMemoizerFactory;

/**
 * Say you have a Java object.  How do you turn it into a byte array for hashing?  This interface knows how.
 * Implement it and pass it {@link MapsMemoizers#create(KeyHandler, PojoBytes) here} or
 * {@link MapsMemoizers#create(PojoBytes) here}.
 */
@SuppressWarnings("unused")
public interface PojoBytes {

    byte[] bytes(Object pojo);

    /**
     * Uses hashcode to derive four bytes. Used by default.
     */
    PojoBytes HASHCODE = MapMemoizerFactory.HASHCODE;

    /**
     * Uses {@link Object#toString()} to derive bytes from the string
     */
    PojoBytes TOSTRING = MapMemoizerFactory.TOSTRING;
}
