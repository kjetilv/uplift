package com.github.kjetilv.uplift.json.mame;

import module java.base;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.json.Token;

abstract sealed class StructureClimber<H extends HashKind<H>>
    extends AbstractClimber<H>
    permits ListClimber, MapClimber {

    private final HashBuilder<byte[], H> builder;

    private final HashBuilder<Hash<H>, H> hashBuilder;

    private final Callbacks parent;

    private final Consumer<HashedTree<String, H>> onDone;

    StructureClimber(
        HashStrategy<H> hashStrategy,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone,
        Callbacks parent
    ) {
        super(hashStrategy, cacher);
        this.onDone = onDone;
        this.parent = parent;
        this.builder = hashStrategy.supplier().get();
        this.hashBuilder = this.builder.map(Hash::bytes);
    }

    @Override
    protected final void done(HashedTree<String, H> tree) {
        hashBuilder.hash(tree.hash());
        cache(tree);
        set(tree);
    }

    protected Callbacks close() {
        var hash = builder.build();
        onDone.accept(hashedTree(hash));
        return parent;
    }

    protected final Token.Field hashedField(Token.Field key) {
        var field = normalized(key);
        builder.hash(fieldBytes(field));
        return field;
    }

    protected abstract HashedTree<String, H> hashedTree(Hash<H> hash);

    protected abstract void set(HashedTree<String, H> tree);
}
