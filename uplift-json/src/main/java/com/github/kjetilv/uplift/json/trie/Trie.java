package com.github.kjetilv.uplift.json.trie;

import com.github.kjetilv.uplift.json.Token;

import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

record Trie(int pos, Token.Field field, IntFunction<Trie> level) {

    public static Trie node(int skip, Token.Field leaf, IntMap<Trie> tries) {
        return new Trie(skip, leaf, tries);
    }

    public Trie descend(int value) {
        return level == null ? null : level.apply(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               Stream.of(
                       ofNullable(pos > 0 ? pos + ">" : null),
                       ofNullable(field).map(f -> "'" + f.value() + "'"),
                       ofNullable(level).map(Objects::toString)
                   )
                   .flatMap(Optional::stream)
                   .collect(Collectors.joining(" ")) +
               "]";
    }
}
