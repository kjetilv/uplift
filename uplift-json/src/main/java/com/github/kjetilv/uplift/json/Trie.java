package com.github.kjetilv.uplift.json;

import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Trie(int skip, Token.Field field, IntFunction<Trie> level) implements IntFunction<Trie> {

    public Trie(int skip, Token.Field field, IntFunction<Trie> level) {
        this.skip = skip;
        this.field = field;
        this.level = level == null ? _ -> null : level;
    }

    public static Trie node(int skip, Token.Field leaf, IntFunction<Trie> tries) {
        return new Trie(skip, leaf, tries);
    }

    @Override
    public Trie apply(int value) {
        return level().apply(value);
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
                       Optional.of(level.toString())
                   )
                   .flatMap(Optional::stream)
                   .collect(Collectors.joining(" ")) +
               "]";
    }
}
