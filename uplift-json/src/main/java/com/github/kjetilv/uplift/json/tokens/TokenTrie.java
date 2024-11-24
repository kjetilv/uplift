package com.github.kjetilv.uplift.json.tokens;

import com.github.kjetilv.uplift.json.Token;

import java.util.ArrayList;
import java.util.Collection;

public class TokenTrie {

    private final int index;

    private final int lower;

    private final Entry[] cached;

    public TokenTrie(Collection<Token> tokens) {
        this.index = 0;
        Collection<Token> ts = new ArrayList<>(tokens);
        this.lower = ts.stream().mapToInt(token -> token.charAt(index)).min().orElseThrow();
        int higher = ts.stream().mapToInt(token -> token.charAt(index)).min().orElseThrow();
        int entryCount = higher - lower + 1;
        this.cached = new Entry[entryCount];

        for (int i = 0; i < cached.length; i++) {

        }
    }

    private record Entry(int c, Token hit, TokenTrie trie) {}
}
