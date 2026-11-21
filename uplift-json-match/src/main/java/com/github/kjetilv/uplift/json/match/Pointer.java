package com.github.kjetilv.uplift.json.match;

import module java.base;

public sealed interface Pointer<T> extends Comparable<Pointer<T>> {

    static <T> Node<T> node(String name, Pointer<T> next, Structure<T> structure) {
        return new PointerNodeImpl<>(name, next, structure);
    }

    static <T> Array<T> array(int index, Pointer<T> pointer, Structure<T> structure) {
        return new PointerArrayImpl<>(index, pointer, structure);
    }

    @SuppressWarnings("unchecked") // Yeah yeah
    static <T> Leaf<T> leaf() {
        return (Leaf<T>) PointerLeafImpl.INSTANCE;
    }

    Optional<T> get(T main);

    Object map(Object leaf);

    non-sealed interface Node<T> extends Pointer<T> {

        String name();

        Pointer<T> next();
    }

    non-sealed interface Array<T> extends Pointer<T> {

        int index();

        Pointer<T> elem();
    }

    non-sealed interface Leaf<T> extends Pointer<T> {
    }
}
