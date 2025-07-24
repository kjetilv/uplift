package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;

public class LeafClimber<H extends HashKind<H>> implements Callbacks {

    private final LeafHasher<H> leafHasher;

    private final Consumer<HashedTree<Token.Field, H>> onDone;

    public LeafClimber(
        LeafHasher<H> leafHasher,
        Consumer<HashedTree<Token.Field, H>> onDone
    ) {
        this.leafHasher = leafHasher;
        this.onDone = onDone;
    }

    @Override
    public Callbacks bool(boolean bool) {
        onDone.accept(TreeClimber.tree(leafHasher, bool));
        return this;
    }

    @Override
    public Callbacks number(Token.Number number) {
        onDone.accept(TreeClimber.tree(leafHasher, number.number()));
        return this;
    }

    @Override
    public Callbacks string(Token.Str str) {
        onDone.accept(TreeClimber.tree(leafHasher, str.value()));
        return this;
    }

}
