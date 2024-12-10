package com.github.kjetilv.uplift.json;

import java.util.Map;

public sealed interface Trie {

    static Trie leaf(Token.Field field) {
        return new Leaf(field);
    }

    static Trie node(int skip, Token.Field field, Map<Character, Trie> characterTrieMap) {
        return new Node(skip, field, characterTrieMap);
    }

    record Leaf(Token.Field field) implements Trie {
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + field + "]";
        }
    }

    record Node(int skip, Token.Field field, Map<Character, Trie> level) implements Trie {
        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" +
                   skip + "> " +
                   (field == null ? "" : field + " ") +
                   level +
                   "]";
        }
    }
}
