package com.github.kjetilv.uplift.fq.data;

import java.util.List;
import java.util.function.Function;

public record Entries<T>(Name name, List<Entry<T>> items) {

    public Entries<T> map(Function<T, T> transform) {
        return new Entries<>(
            name, items.stream()
            .map(item ->
                item.map(transform))
            .toList()
        );
    }
}
