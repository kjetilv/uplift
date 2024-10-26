package org.slf4j;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class MarkerFactory {

    public static Marker getMarker(String name) {
        return new MarkerImpl(name);
    }

    public static Marker getDetachedMarker(String name) {
        return new MarkerImpl(name);
    }

    public static IMarkerFactory getIMarkerFactory() {
        return MarkerFactory::getMarker;
    }

    private MarkerFactory() {
    }
}
