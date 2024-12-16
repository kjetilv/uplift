package com.github.kjetilv.uplift.json;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.kjetilv.uplift.json.Trie.node;

public class TokenTrie implements Function<char[], Token.Field> {

    private final Trie root;

    public TokenTrie(Token.Field... fields) {
        this(List.of(fields));
    }

    public TokenTrie(Collection<Token.Field> fields) {
        root = trie(fields, 0).toNode();
    }

    @Override
    public Token.Field apply(char[] chars) {
        return get(chars);
    }

    public Token.Field get(char[] chars) {
        Trie trie = this.root;
        while (true) {
            switch (trie) {
                case null -> {
                    return new Token.Field(chars);
                }
                case Trie.Leaf(Token.Field field) -> {
                    return field.is(chars)
                        ? field
                        : new Token.Field(chars);
                }
                case Trie.Node(int skip, Token.Field field, Map<Character, Trie> level) -> {
                    if (chars.length == skip) {
                        return field;
                    }
                    if (chars.length <= skip) {
                        return new Token.Field(chars);
                    }
                    trie = level.get(chars[skip]);
                }
            }
        }
    }

    private static Level trie(Collection<Token.Field> fields, int index) {

        Map<Character, List<Token.Field>> groups = fields.stream()
            .filter(field -> field.chars().length > index)
            .collect(Collectors.groupingBy(field ->
                field.chars()[index]));

        List<Entry<Token.Field>> leafFields = entries(groups)
            .filter(entry ->
                entry.value().size() == 1)
            .map(entry -> entry.map(List::getFirst))
            .toList();

        List<Entry<Trie>> leafTries = leafFields
            .stream()
            .map(entry ->
                entry.map(Trie::leaf))
            .toList();

        List<Entry<Level>> nextLevels = entries(groups)
            .filter(isNode(leafFields))
            .filter(entry ->
                entry.value().size() > 1)
            .map(entry ->
                entry.map((_, prefixed) -> trie(
                    prefixed,
                    index + shortestPrefix(index, prefixed)
                )))
            .toList();

        Token.Field leaf = fields.stream()
            .filter(f -> f.chars().length == index)
            .findFirst().orElse(null);

        return new Level(index, leaf, leafTries, nextLevels);
    }

    private static Predicate<Entry<List<Token.Field>>> isNode(List<Entry<Token.Field>> leafFields) {
        Collection<Character> leafChars = leafFields
            .stream()
            .map(Entry::key)
            .collect(Collectors.toSet());
        return entry ->
            !leafChars.contains(entry.key);
    }

    private static <T> Stream<Entry<T>> entries(Map<Character, T> groups) {
        return groups.entrySet()
            .stream()
            .map(Entry::of);
    }

    private static int shortestPrefix(
        int startingIndex,
        List<Token.Field> prefixed
    ) {
        int shortestLength = prefixed.stream()
            .min(Comparator.comparingInt(f -> f.chars().length))
            .map(Token.Field::chars).orElseThrow().length;

        List<char[]> charses = prefixed.stream()
            .map(Token.Field::chars)
            .toList();

        for (int i = startingIndex; i < shortestLength; i++) {
            if (!sameCharAt(i, charses)) {
                return i - startingIndex;
            }
        }
        return shortestLength;
    }

    private static boolean sameCharAt(int fi, List<char[]> charses) {
        return charses.stream().mapToInt(chars -> chars[fi]).distinct().count() == 1;
    }

    private static <T> Map<Character, T> mapEntries(Stream<Entry<T>> entries) {
        return entries.collect(Collectors.toMap(Entry::key, Entry::value));
    }

    private record Entry<T>(char key, T value) {

        static <T> Entry<T> of(Map.Entry<Character, T> entry) {
            return of(entry.getKey(), entry.getValue());
        }

        static <T> Entry<T> of(Character key, T value) {
            return new Entry<>(key, value);
        }

        <R> Entry<R> map(BiFunction<Character, T, R> mapper) {
            return Entry.of(key, mapper.apply(key, value));
        }

        <R> Entry<R> map(Function<T, R> mapper) {
            return Entry.of(key, mapper.apply(value));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + key + "=" + value + "]";
        }
    }

    private record Level(int skip, Token.Field field, List<Entry<Trie>> leaves, List<Entry<Level>> levels) {

        Trie toNode() {
            return node(
                skip,
                field,
                mapEntries(nextLevel())
            );
        }

        private Stream<Entry<Trie>> nextLevel() {
            return Stream.concat(
                leaves.stream(),
                levels.stream()
                    .map(entry -> entry.map(Level::toNode))
            );
        }
    }
}
