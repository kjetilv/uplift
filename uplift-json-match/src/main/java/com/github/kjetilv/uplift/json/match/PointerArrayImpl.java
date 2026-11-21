package com.github.kjetilv.uplift.json.match;

import module java.base;

record PointerArrayImpl<T>(int index, Pointer<T> elem, Structure<T> structure) implements Pointer.Array<T>, NameChain {

    @Override
    public Optional<T> get(T main) {
        return structure.get(main, index);
    }

    @Override
    public Object map(Object leaf) {
        List<Object> list = new ArrayList<>(index + 1);
        if (index > 0) {
            for (var i = 0; i < index; i++) {
                list.add(null);
            }
        }
        list.add(elem.map(leaf));
        return list;
    }

    @Override
    public Stream<String> get() {
        return elem instanceof NameChain nameChain
            ? Stream.concat(elementStream(), nameChain.get())
            : elementStream();
    }

    @Override
    public int compareTo(Pointer<T> pointer) {
        return pointer instanceof Pointer.Array<T> array
            ? Integer.compare(index(), array.index())
            : -1;
    }

    private Stream<String> elementStream() {
        return IntStream.of(index).mapToObj(Integer::toString);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + index + ": " + elem + "]";
    }
}
