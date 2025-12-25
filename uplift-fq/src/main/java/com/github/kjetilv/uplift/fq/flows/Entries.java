package com.github.kjetilv.uplift.fq.flows;

import java.util.List;
import java.util.function.Function;

public record Entries<T>(Name name, List<T> items) {

    public static <T> Entries<T> single(Name name, T item) {
        return new Entries<>(name, List.of(item));
    }

    public static <T> Entries<T> of(Name name, List<T> items) {
        return new Entries<>(name, List.copyOf(items));
    }

    public Entries<T> map(Function<T, T> transform) {
        var list = items.stream()
            .map(transform)
            .toList();
        return new Entries<>(name, list);
    }

    public boolean matches(Entries<T> items) {
        return size() == items.size();
    }

    public int size() {
        return items.size();
    }
}
