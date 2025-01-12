package com.github.kjetilv.uplift.json;

import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Trie(int skip, Token.Field field, IntFunction<Trie> level) {

    public static Trie node(int skip, Token.Field leaf, IntFunction<Trie> tries) {
        return new Trie(skip, leaf, tries);
    }

    public Trie descend(byte b) {
        return level == null ? null : level.apply(b);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               Stream.of(
                       Optional.of(skip)
                           .filter(s -> s > 0)
                           .map(s -> s + ">"),
                       Optional.ofNullable(field)
                           .map(f -> "'" + f.value() + "'"),
                       Optional.ofNullable(level)
                           .map(Objects::toString)
                   )
                   .flatMap(Optional::stream)
                   .collect(Collectors.joining(" ")) +
               "]";
    }
}
