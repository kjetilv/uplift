package org.slf4j;

import java.util.Iterator;

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
