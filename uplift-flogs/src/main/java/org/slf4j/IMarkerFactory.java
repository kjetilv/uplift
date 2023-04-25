package org.slf4j;

@FunctionalInterface
@SuppressWarnings("unused")
public interface IMarkerFactory {

    Marker getMarker(String name);

    default boolean exists(String name) {
        return false;
    }

    default boolean detachMarker(String name) {
        return false;
    }

    default Marker getDetachedMarker(String name) {
        return getMarker(name);
    }
}
