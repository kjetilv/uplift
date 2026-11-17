package org.apache.logging.log4j;

/// Log4j markers. Flogs does not use markers when it logs.
@SuppressWarnings("unused")
public interface Marker {

    Marker addParents(Marker... markers);

    String getName();

    Marker[] getParents();

    boolean hasParents();

    boolean isInstanceOf(Marker m);

    boolean isInstanceOf(String name);

    boolean remove(Marker marker);

    Marker setParents(Marker... markers);

    boolean equals(Object obj);

    int hashCode();
}
