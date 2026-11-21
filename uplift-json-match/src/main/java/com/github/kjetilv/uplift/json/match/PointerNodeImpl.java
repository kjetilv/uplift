package com.github.kjetilv.uplift.json.match;

import module java.base;

record PointerNodeImpl<T>(String name, Pointer<T> next, Structure<T> structure) implements Pointer.Node<T>, NameChain {

    @Override
    public Optional<T> get(T main) {
        return structure.get(main, name).flatMap(next::get);
    }

    @Override
    public Map<String, ?> map(Object leaf) {
        return Map.of(name, next.map(leaf));
    }

    @Override
    public Stream<String> get() {
        return Stream.concat(
            Stream.of(name()),
            next() instanceof NameChain nameChain ? nameChain.get() : Stream.empty()
        );
    }

    @Override
    public int compareTo(Pointer<T> pointer) {
        if (pointer instanceof NameChain chain) {
            return path().compareTo(chain.path());
        }
        return 1;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name + ": " + next + "]";
    }
}
