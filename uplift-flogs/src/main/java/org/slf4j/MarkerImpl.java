package org.slf4j;

import module java.base;

public record MarkerImpl(String name) implements Marker {

    public MarkerImpl {
        Objects.requireNonNull(name, "name");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void add(Marker reference) {
    }

    @Override
    public boolean remove(Marker reference) {
        return false;
    }

    @Override
    @Deprecated
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean hasReferences() {
        return false;
    }

    @Override
    public Iterator<Marker> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean contains(Marker other) {
        return false;
    }

    @Override
    public boolean contains(String name) {
        return false;
    }
}
