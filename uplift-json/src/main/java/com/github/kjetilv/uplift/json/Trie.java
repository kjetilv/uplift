package com.github.kjetilv.uplift.json;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Trie(int skip, Token.Field field, Map<Byte, Trie> level) {

    public static Trie node(int skip, Token.Field leaf, Map<Byte, Trie> tries) {
        return new Trie(skip, leaf, tries);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + Stream.of(
                Optional.of(skip)
                    .filter(s -> s > 0)
                    .map(s -> s + ">"),
                Optional.ofNullable(field)
                    .map(f -> "'" + f.value() + "'"),
                Optional.ofNullable(level)
                    .filter(l -> !l.isEmpty())
                    .map(l ->
                        l.entrySet()
                            .stream()
                            .map(entry ->
                                "'" + entry.getKey() + "':" + entry.getValue())
                            .collect(Collectors.joining("/")))
            ).flatMap(Optional::stream)
            .collect(Collectors.joining(" ")) + "]";
    }
}
