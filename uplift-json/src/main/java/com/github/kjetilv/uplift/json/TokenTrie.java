package com.github.kjetilv.uplift.json;

import com.github.kjetilv.flopp.kernel.LineSegment;
import com.github.kjetilv.flopp.kernel.LineSegments;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TokenTrie implements TokenResolver {

    private final Trie root;

    private final int size;

    public TokenTrie(Token.Field... fields) {
        this(List.of(fields));
    }

    public TokenTrie(Collection<Token.Field> fields) {
        List<Token.Field> copy = List.copyOf(fields);
        this.size = copy.size();
        this.root = buildTrie(copy, 0);
    }

    public Token.Field get(Token.Field token) {
        return get(token.lineSegment());
    }

    public Token.Field get(String token) {
        return get(LineSegments.of(token));
    }

    @Override
    public Token.Field get(LineSegment lineSegment) {
        Trie trie = this.root;
        long length = lineSegment.length();
        while (true) {
            if (trie instanceof Trie(int skip, Token.Field field, Map<Byte, Trie> level)) {
                if (length == skip) {
                    return field;
                }
                if (length <= skip) {
                    return null;
                }
                byte c = lineSegment.byteAt(skip);
                trie = level.get(c);
            } else {
                return null;
            }
        }
    }

    private static Trie buildTrie(Collection<Token.Field> fields, int index) {
        Token.Field leaf = fields.stream()
            .filter(f -> f.length() == index)
            .findFirst()
            .orElse(null);

        if (leaf != null && fields.size() == 1) {
            return Trie.node(leaf.length(), leaf, Collections.emptyMap());
        }

        Map<Byte, List<Token.Field>> groups = fields.stream()
            .filter(f -> f.length() > index)
            .collect(Collectors.groupingBy(field ->
                field.lineSegment().byteAt(index)));

        List<Entry<Trie>> nextLevels = entries(groups)
            .map(entry ->
                entry.map((_, prefixed) -> {
                    int nextIndex = longestCommonPrefix(prefixed);
                    return buildTrie(prefixed, nextIndex);
                }))
            .toList();

        return Trie.node(
            index,
            leaf,
            nextLevels.stream()
                .collect(Collectors.toMap(
                    Entry::key,
                    Entry::value,
                    (trie1, trie2) -> {
                        throw new IllegalStateException("No combine: " + trie1 + " and " + trie2);
                    },
                    LinkedHashMap::new
                ))
        );
    }

    private static <T> Stream<Entry<T>> entries(Map<Byte, T> groups) {
        return groups.entrySet()
            .stream()
            .map(Entry::of);
    }

    private static int longestCommonPrefix(
        List<Token.Field> fields
    ) {
        if (fields.size() == 1) {
            return fields.getFirst().length();
        }
        int shortest = fields.stream()
            .min(Comparator.comparing(Token.Field::length))
            .map(Token.Field::length)
            .orElseThrow();
        for (int i = 0; i < shortest; i++) {
            if (!sameCharAt(i, fields)) {
                return i;
            }
        }
        return shortest;
    }

    private static boolean sameCharAt(int index, List<Token.Field> fields) {
        Token.Field init = fields.getFirst();
        for (int i = 1; i < fields.size(); i++) {
            if (fields.get(i).differsAt(init, index)) {
                return false;
            }
        }
        return true;
    }

    private record Entry<T>(byte key, T value) {

        static <T> Entry<T> of(Map.Entry<Byte, T> entry) {
            return of(entry.getKey(), entry.getValue());
        }

        static <T> Entry<T> of(byte key, T value) {
            return new Entry<>(key, value);
        }

        <R> Entry<R> map(BiFunction<Byte, T, R> mapper) {
            return Entry.of(key, mapper.apply(key, value));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + key + "=" + value + "]";
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + size + "]";
    }
}
