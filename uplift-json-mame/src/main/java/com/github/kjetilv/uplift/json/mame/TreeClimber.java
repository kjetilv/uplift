package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.CanonicalValue;
import com.github.kjetilv.uplift.edamame.Canonicalizer;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record TreeClimber<H extends HashKind<H>>(
    Supplier<HashBuilder<byte[], H>> hashBuilderSupplier,
    LeafHasher<H> leafHasher,
    Canonicalizer<String, H> cacher,
    Consumer<Object> onDone
) implements Callbacks {

    static <H extends HashKind<H>> HashedTree.Leaf<String, H> tree(
        LeafHasher<H> leafHasher,
        Object leaf
    ) {
        return new HashedTree.Leaf<>(leafHasher.hash(leaf), leaf);
    }

    @Override
    public Callbacks bool(boolean bool) {
        done(TreeClimber.tree(leafHasher, bool));
        return this;
    }

    @Override
    public Callbacks number(Token.Number number) {
        done(TreeClimber.tree(leafHasher, number.number()));
        return this;
    }

    @Override
    public Callbacks string(Token.Str str) {
        done(TreeClimber.tree(leafHasher, str.value()));
        return this;
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListClimber<>(
            hashBuilderSupplier,
            leafHasher,
            this,
            cacher::canonical,
            this::done
        );
    }

    @Override
    public Callbacks objectStarted() {
        return new MapClimber<>(
            hashBuilderSupplier,
            leafHasher,
            this,
            cacher::canonical,
            this::done
        );
    }

    private void done(HashedTree<String, H> tree) {
        CanonicalValue<H> canonical = cacher.canonical(tree);
        onDone.accept(canonical.value());
    }
}
