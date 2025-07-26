package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.LeafHasher;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;
import java.util.function.Supplier;

sealed abstract class SubClimber<H extends HashKind<H>> extends AbstractClimber<H>
    permits ListClimber, MapClimber {

    private final HashBuilder<byte[], H> builder;

    private final HashBuilder<Hash<H>, H> hashBuilder;

    private final Callbacks parent;

    private final Consumer<HashedTree<String, H>> onDone;

    SubClimber(
        Supplier<HashBuilder<byte[], H>> supplier,
        LeafHasher<H> leafHasher,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone,
        Callbacks parent
    ) {
        super(supplier, leafHasher, cacher);
        this.onDone = onDone;
        this.parent = parent;

        this.builder = supplier.get();
        this.hashBuilder = this.builder.map(Hash::bytes);
    }

    @Override
    public final Callbacks field(Token.Field key) {
        if (this instanceof MapClimber) {
            Token.Field field = normalized(key);
            builder.hash(fieldBytes(field));
            fieldWasSet(field);
            return this;
        }
        throw new IllegalStateException("Unexecpted event");
    }

    @Override
    protected final Callbacks done(HashedTree<String, H> tree) {
        hashBuilder.hash(tree.hash());
        cacher.accept(tree);
        add(tree);
        return this;
    }

    protected void fieldWasSet(Token.Field field) {
        throw new IllegalStateException("Unexpected event: " + field);
    }

    protected Callbacks close() {
        Hash<H> hash = builder.get();
        onDone.accept(hashedTree(hash));
        return parent;
    }

    protected abstract HashedTree<String, H> hashedTree(Hash<H> hash);

    protected abstract void add(HashedTree<String, H> tree);
}
