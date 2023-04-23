
package org.slf4j;

import java.util.Iterator;

@SuppressWarnings("unused")
public interface Marker {

    /**
     * Get the name of this Marker.
     *
     * @return name of marker
     */
    String getName();

    void add(Marker reference);

    boolean remove(Marker reference);

    @Deprecated
    boolean hasChildren();

    boolean hasReferences();

    Iterator<Marker> iterator();

    boolean contains(Marker other);

    boolean contains(String name);

    boolean equals(Object o);
    int hashCode();
}
