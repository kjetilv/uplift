package org.slf4j;

import module java.base;

@SuppressWarnings("unused")
public interface Marker {

    /// @return name of this Marker
    String getName();

    void add(Marker reference);

    boolean remove(Marker reference);

    @Deprecated
    boolean hasChildren();

    boolean hasReferences();

    Iterator<Marker> iterator();

    boolean contains(Marker other);

    boolean contains(String name);

    int hashCode();

    boolean equals(Object o);
}
