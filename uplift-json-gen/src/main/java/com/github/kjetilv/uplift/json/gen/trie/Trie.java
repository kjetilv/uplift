package com.github.kjetilv.uplift.json.gen.trie;

import com.github.kjetilv.uplift.json.Token;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

record Trie(int pos, Token.Field field, IntMap<Trie> level) {

    public static Trie node(int pos, Token.Field field, IntMap<Trie> level) {
        return new Trie(pos, field, level);
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
