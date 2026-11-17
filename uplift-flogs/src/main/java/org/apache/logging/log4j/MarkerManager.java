package org.apache.logging.log4j;

import module java.base;

/// Gives one marker for each name. The markers have no parents: the operations that change parents have no
/// effect.
@SuppressWarnings({"unused", "WeakerAccess"})
public final class MarkerManager {

    public static Marker getMarker(String name) {
        return MARKERS.computeIfAbsent(Objects.requireNonNull(name, "name"), Log4jMarker::new);
    }

    /// @deprecated The parent is not used. Use [#getMarker(String)].
    @Deprecated
    public static Marker getMarker(String name, String parent) {
        return getMarker(name);
    }

    /// @deprecated The parent is not used. Use [#getMarker(String)].
    @Deprecated
    public static Marker getMarker(String name, Marker parent) {
        return getMarker(name);
    }

    public static boolean exists(String key) {
        return key != null && MARKERS.containsKey(key);
    }

    public static void clear() {
        MARKERS.clear();
    }

    private MarkerManager() {
    }

    private static final Map<String, Marker> MARKERS = new ConcurrentHashMap<>();

    public record Log4jMarker(String name) implements Marker {

        public Log4jMarker {
            Objects.requireNonNull(name, "name");
        }

        @Override
        public Marker addParents(Marker... markers) {
            return this;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Marker[] getParents() {
            return NO_PARENTS;
        }

        @Override
        public boolean hasParents() {
            return false;
        }

        @Override
        public boolean isInstanceOf(Marker m) {
            return equals(m);
        }

        @Override
        public boolean isInstanceOf(String name) {
            return this.name.equals(name);
        }

        @Override
        public boolean remove(Marker marker) {
            return false;
        }

        @Override
        public Marker setParents(Marker... markers) {
            return this;
        }

        private static final Marker[] NO_PARENTS = {};
    }
}
