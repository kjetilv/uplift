package com.github.kjetilv.uplift.json.gen.trie;

import com.github.kjetilv.uplift.json.Token;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.github.kjetilv.uplift.json.gen.trie.Trie.node;

final class TrieBuilder {

    static Trie build(Collection<Token.Field> fields) {
        return build(List.copyOf(fields), 0);
    }

    private TrieBuilder() {
    }

    private static Trie build(Collection<Token.Field> fields, int index) {
        Token.Field leaf = fields.stream()
            .filter(f -> f.length() == index)
            .findFirst()
            .orElse(null);

        if (leaf != null && fields.size() == 1) {
            return node(leaf.length(), leaf, null);
        }
        Map<Byte, Trie> level = nextLevel(fields, index);
        return node(index, leaf, IntMap.from(level));
    }

    private static Map<Byte, Trie> nextLevel(Collection<Token.Field> fields, int index) {
        return level(mapEntries(fields, index).stream()
            .map(TrieBuilder::tokenEntry)
            .map(TrieBuilder::trieEntry)
            .toList());
    }

    private static Set<Map.Entry<Byte, List<Token.Field>>> mapEntries(Collection<Token.Field> fields, int index) {
        return fields.stream()
            .filter(f -> f.length() > index)
            .collect(Collectors.groupingBy(field ->
                field.bytes()[index]))
            .entrySet();
    }

    private static Entry<Trie> trieEntry(Entry<List<Token.Field>> entry) {
        return entry.map((_, prefixed) ->
            build(prefixed, longestCommonPrefix(prefixed)));
    }

    private static Entry<List<Token.Field>> tokenEntry(Map.Entry<Byte, List<Token.Field>> e) {
        return new Entry<>(e.getKey(), e.getValue());
    }

    private static Map<Byte, Trie> level(List<Entry<Trie>> nextLevels) {
        return nextLevels.stream()
            .collect(Collectors.toMap(Entry::key, Entry::value));
    }

    private static int longestCommonPrefix(List<Token.Field> fields) {
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

        <R> Entry<R> map(BiFunction<Byte, T, R> mapper) {
            return new Entry<>(key, mapper.apply(key, value));
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + key + "=" + value + "]";
        }
    }
}
