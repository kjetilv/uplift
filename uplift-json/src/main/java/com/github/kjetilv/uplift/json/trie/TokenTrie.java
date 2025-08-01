package com.github.kjetilv.uplift.json.trie;

import com.github.kjetilv.uplift.json.Token;
import com.github.kjetilv.uplift.json.TokenResolver;

import java.util.Collection;
import java.util.List;
import java.util.function.LongToIntFunction;

public class TokenTrie implements TokenResolver {

    private final Trie root;

    private final int size;

    public TokenTrie(Token.Field... fields) {
        this(List.of(fields));
    }

    public TokenTrie(Collection<Token.Field> fields) {
        this.size = fields.size();
        this.root = TrieBuilder.build(fields);
    }

    @Override
    public Token.Field get(LongToIntFunction bytes, long offset, long length) {
        Trie t = this.root;
        do {
            if (t == null) {
                return null;
            }
            int pos = t.pos();
            if (length == pos) {
                return t.field();
            }
            if (length < pos) {
                return null;
            }
            int b = bytes.applyAsInt(offset + pos);
            t = t.descend(b);
        } while (true);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + size + "]";
    }
}
