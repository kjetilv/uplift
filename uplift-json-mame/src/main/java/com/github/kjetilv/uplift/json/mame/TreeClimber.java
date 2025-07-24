package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.*;
import com.github.kjetilv.uplift.hash.*;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TreeClimber<H extends HashKind<H>> implements Callbacks {

    public static <H extends HashKind<H>, K> Callbacks climb(
        H kind,
        Consumer<HashedTree<Token.Field, H>> onDone
    ) {
        Supplier<HashBuilder<Bytes, H>> supplier = () -> Hashes.hashBuilder(kind);
        LeafHasher<H> leafHasher = LeafHasher.create(kind, PojoBytes.HASHCODE);
        MapsMemoizer<Object, String> objectStringMapsMemoizer = MapsMemoizers.create(kind);
        return new TreeClimber<>(supplier, leafHasher, onDone);
    }

    public static <H extends HashKind<H>> HashedTree.Leaf<Token.Field, H> tree(
        LeafHasher<H> leafHasher, Object leaf
    ) {
        Hash<H> hash = leafHasher.hash(leaf);
        HashedTree.Leaf<Token.Field, H> t = new HashedTree.Leaf<>(hash, leaf);
        return t;
    }

    private final Supplier<HashBuilder<byte[], H>> hashBuilderSupplier;

    private final LeafHasher<H> leafHasher;

    private final Consumer<HashedTree<Token.Field, H>> cacher;

    public TreeClimber(
        Supplier<HashBuilder<Bytes, H>> hashBuilderSupplier,
        LeafHasher<H> leafHasher,
        Consumer<HashedTree<Token.Field, H>> cacher
    ) {
        this.hashBuilderSupplier = () -> hashBuilderSupplier.get().map(Bytes::from);
        this.leafHasher = leafHasher;
        this.cacher = cacher;
    }

    @Override
    public Callbacks bool(boolean bool) {
        cacher.accept(TreeClimber.tree(leafHasher, bool));
        return this;
    }

    @Override
    public Callbacks number(Token.Number number) {
        cacher.accept(TreeClimber.tree(leafHasher, number.number()));
        return this;
    }

    @Override
    public Callbacks string(Token.Str str) {
        cacher.accept(TreeClimber.tree(leafHasher, str.value()));
        return this;
    }

    @Override
    public Callbacks arrayStarted() {
        return new ListClimber<>(
            hashBuilderSupplier,
            leafHasher,
            this,
            cacher,
            _ -> {
            }
        );
    }

    @Override
    public Callbacks objectStarted() {
        return new MapClimber<>(
            hashBuilderSupplier,
            leafHasher,
            this,
            cacher,
            _ -> {
            }
        );
    }
}
