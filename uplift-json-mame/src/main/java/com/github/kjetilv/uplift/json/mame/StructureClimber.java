package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

import java.util.function.Consumer;

sealed abstract class StructureClimber<H extends HashKind<H>>
    extends AbstractClimber<H>
    permits ListClimber, MapClimber {

    private final HashBuilder<byte[], H> builder;

    private final HashBuilder<Hash<H>, H> hashBuilder;

    private final Callbacks parent;

    private final Consumer<HashedTree<String, H>> onDone;

    StructureClimber(
        Strategy<H> strategy,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone,
        Callbacks parent
    ) {
        super(strategy, cacher);
        this.onDone = onDone;
        this.parent = parent;
        this.builder = strategy.supplier().get();
        this.hashBuilder = this.builder.map(Hash::bytes);
    }

    @Override
    protected final void done(HashedTree<String, H> tree) {
        hashBuilder.hash(tree.hash());
        cacher.accept(tree);
        set(tree);
    }

    protected Callbacks close() {
        Hash<H> hash = builder.build();
        onDone.accept(hashedTree(hash));
        return parent;
    }

    protected final Token.Field hashedField(Token.Field key) {
        Token.Field field = normalized(key);
        builder.hash(fieldBytes(field));
        return field;
    }

    protected abstract HashedTree<String, H> hashedTree(Hash<H> hash);

    protected abstract void set(HashedTree<String, H> tree);
}
