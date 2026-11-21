package com.github.kjetilv.uplift.json.match;

import module java.base;

record PointerLeafImpl<T>() implements Pointer.Leaf<T> {

    @Override
    public Optional<T> get(T t) {
        return Optional.of(t);
    }

    @Override
    public Object map(Object leaf) {
        return leaf;
    }

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    @Override
    public int compareTo(Pointer<T> pointer) {
        return -1;
    }

    static final Leaf<?> INSTANCE = new PointerLeafImpl<>();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
}
