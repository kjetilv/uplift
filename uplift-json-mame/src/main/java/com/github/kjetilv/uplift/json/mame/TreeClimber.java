package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.*;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
public class TreeClimber<H extends HashKind<H>> implements Callbacks {

    public static <H extends HashKind<H>> HashedTree.Leaf<Token.Field, H> tree(
        LeafHasher<H> leafHasher, Object leaf
    ) {
        Hash<H> hash = leafHasher.hash(leaf);
        return new HashedTree.Leaf<>(hash, leaf);
    }

    private final Supplier<HashBuilder<byte[], H>> hashBuilderSupplier;

    private final LeafHasher<H> leafHasher;

    private final Canonicalizer<Token.Field, H> cacher;

    private final Consumer<Object> onDone;

    public TreeClimber(
        Supplier<HashBuilder<Bytes, H>> hashBuilderSupplier,
        LeafHasher<H> leafHasher,
        Canonicalizer<Token.Field, H> cacher,
        Consumer<Object> onDone
    ) {
        this.hashBuilderSupplier = () -> hashBuilderSupplier.get()
            .map(Bytes::from);
        this.leafHasher = leafHasher;
        this.cacher = cacher;
        this.onDone = onDone;
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

    private void done(HashedTree<Token.Field, H> tree) {
        CanonicalValue<H> canonical = cacher.canonical(tree);
        onDone.accept(canonical.value());
    }
}
